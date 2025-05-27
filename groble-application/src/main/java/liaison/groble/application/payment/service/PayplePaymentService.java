package liaison.groble.application.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.payment.dto.BillingPaymentRequest;
import liaison.groble.application.payment.dto.PaymentCancelResponseDto;
import liaison.groble.application.payment.dto.PaymentCompleteResponseDto;
import liaison.groble.application.payment.dto.PaymentInfoDto;
import liaison.groble.application.payment.dto.PaymentRequestDto;
import liaison.groble.application.payment.dto.PaymentRequestResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
import liaison.groble.application.payment.dto.PaypleLinkResponseDto;
import liaison.groble.application.payment.dto.PayplePaymentLinkRequestDto;
import liaison.groble.application.payment.dto.PayplePaymentResultDto;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.external.adapter.payment.PayplePayInfoRequest;
import liaison.groble.external.adapter.payment.PaypleRefundRequest;
import liaison.groble.external.adapter.payment.PaypleService;
import liaison.groble.external.adapter.payment.PaypleSimplePayRequest;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayplePaymentService {
  private final PaypleConfig paypleConfig;
  private final PaypleService paypleService;
  private final PayplePaymentRepository payplePaymentRepository;
  private final OrderRepository orderRepository;

  public PaypleLinkResponseDto processLinkPayment(
      PayplePaymentLinkRequestDto payplePaymentLinkRequestDto,
      PaypleAuthResponseDto paypleAuthResponseDto) {
    // 주문 요청자와 결제 요청자 동일 여부 검증 필요

    Long orderId = payplePaymentLinkRequestDto.getOrderId();
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();

    Map<String, String> params = new HashMap<>();
    if (paypleAuthResponseDto != null) {
      params.put("PCD_AUTH_KEY", paypleAuthResponseDto.getAuthKey());
      params.put("PCD_PAY_WORK", "LINKREG");
      params.put("PCD_PAY_GOODS", contentTitle);
    }

    Map<String, BigDecimal> amounts = new HashMap<>();
    amounts.put("PCD_PAY_TOTAL", order.getFinalAmount());

    JSONObject linkPayResult = paypleService.payLinkCreate(params, amounts);

    return PaypleLinkResponseDto.builder()
        .paymentResult((String) linkPayResult.get("PCD_PAY_RST"))
        .paymentLinkUrl((String) linkPayResult.get("PCD_LINK_URL"))
        .build();
  }

  /** 결제 인증 정보 조회 */
  // payWork
  public PaypleAuthResponseDto getPaymentAuth(String payWork) {
    Map<String, String> params = new HashMap<>();
    if (payWork != null) {
      params.put("PCD_PAY_WORK", payWork);
    }

    JSONObject authResult = paypleService.payAuth(params);

    return PaypleAuthResponseDto.builder()
        .clientKey(paypleConfig.getClientKey())
        .authKey((String) authResult.get("AuthKey"))
        .returnUrl((String) authResult.get("return_url"))
        .result((String) authResult.get("result"))
        .resultMsg((String) authResult.get("result_msg"))
        .build();
  }

  /** 결제 요청 처리 */
  @Transactional
  public PaymentRequestResponseDto processPayment(
      Long userId, PaymentRequestDto paymentRequestDto) {
    // 주문번호 생성
    String orderId = generateOrderId();

    // Payment 엔티티 생성 및 저장
    PayplePayment payment =
        PayplePayment.builder()
            .orderId(orderId)
            .userId(userId)
            .amount(paymentRequestDto.getAmount())
            .payMethod(paymentRequestDto.getPayMethod())
            .status(PayplePaymentStatus.PENDING)
            .productName(paymentRequestDto.getProductName())
            .build();

    PayplePayment savedPayplePayment = payplePaymentRepository.save(payment);

    return PaymentRequestResponseDto.from(savedPayplePayment);
  }

  /** 결제 완료 처리 */
  @Transactional
  public PaymentCompleteResponseDto completePayment(PayplePaymentResultDto payplePaymentResultDto) {
    PayplePayment payment =
        payplePaymentRepository
            .findByOrderId(payplePaymentResultDto.getPayOid())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

    if ("success".equals(payplePaymentResultDto.getPayRst())) {
      payment.complete(
          payplePaymentResultDto.getPayerId(),
          payplePaymentResultDto.getPayTime(),
          payplePaymentResultDto.getPayCardName(),
          payplePaymentResultDto.getPayCardNum());
    } else {
      payment.fail(payplePaymentResultDto.getPayMsg());
    }

    return PaymentCompleteResponseDto.from(payplePaymentRepository.save(payment));
  }

  /** 결제 취소 */
  @Transactional
  public PaymentCancelResponseDto cancelPayment(String orderId, String reason) {
    PayplePayment payment =
        payplePaymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

    // 결제 취소 API 호출
    PaypleRefundRequest refundRequest =
        PaypleRefundRequest.builder()
            .payOid(payment.getOrderId())
            .payDate(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .refundTotal(payment.getAmount().toString())
            .build();

    JSONObject refundResult = paypleService.payRefund(refundRequest);

    if ("success".equals(refundResult.get("PCD_PAY_RST"))) {
      payment.cancel(reason);
      return PaymentCancelResponseDto.from(payplePaymentRepository.save(payment));
    } else {
      throw new RuntimeException("결제 취소 실패: " + refundResult.get("PCD_PAY_MSG"));
    }
  }

  /** 정기결제 (빌링) 실행 */
  @Transactional
  public PayplePayment processBillingPayment(BillingPaymentRequest request) {
    // Payment 엔티티 생성
    String orderId = generateOrderId();
    PayplePayment payment =
        PayplePayment.builder()
            .orderId(orderId)
            .userId(request.getUserId())
            .amount(request.getAmount())
            .payMethod("CARD")
            .status(PayplePaymentStatus.PENDING)
            .productName(request.getProductName())
            .billingKey(request.getBillingKey())
            .build();

    payment = payplePaymentRepository.save(payment);

    // 빌링 결제 실행
    PaypleSimplePayRequest simplePayRequest =
        PaypleSimplePayRequest.builder()
            .payType("card")
            .payerId(request.getBillingKey())
            .payGoods(request.getProductName())
            .payTotal(request.getAmount().toString())
            .payOid(orderId)
            .payerNo(request.getUserId().toString())
            .payerName(request.getUserName())
            .payerHp(request.getUserPhone())
            .payerEmail(request.getUserEmail())
            .payIstax("Y")
            .build();

    JSONObject result = paypleService.paySimple(simplePayRequest);

    if ("success".equals(result.get("PCD_PAY_RST"))) {
      payment.complete(
          (String) result.get("PCD_PAYER_ID"),
          (String) result.get("PCD_PAY_TIME"),
          (String) result.get("PCD_PAY_CARDNAME"),
          (String) result.get("PCD_PAY_CARDNUM"));
    } else {
      payment.fail((String) result.get("PCD_PAY_MSG"));
    }

    return payplePaymentRepository.save(payment);
  }

  /** 결제 정보 조회 */
  public PaymentInfoDto getPaymentInfo(String orderId) {
    PayplePayment payment =
        payplePaymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

    // Payple API를 통한 결제 정보 조회
    PayplePayInfoRequest request =
        PayplePayInfoRequest.builder()
            .payType(payment.getPayMethod().toLowerCase())
            .payOid(payment.getOrderId())
            .payDate(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .build();

    JSONObject payInfo = paypleService.payInfo(request);

    return PaymentInfoDto.from(payment, payInfo);
  }

  /** 빌링키 해지 */
  @Transactional
  public void deleteBillingKey(Long userId, String billingKey) {
    // 빌링키 해지 API 호출
    JSONObject result = paypleService.payUserDel(billingKey);

    if (!"success".equals(result.get("PCD_PAY_RST"))) {
      throw new RuntimeException("빌링키 해지 실패: " + result.get("PCD_PAY_MSG"));
    }

    // 해당 빌링키를 사용하는 모든 결제 정보 업데이트
    payplePaymentRepository
        .findByUserIdAndBillingKey(userId, billingKey)
        .forEach(
            payment -> {
              payment.invalidateBillingKey();
              payplePaymentRepository.save(payment);
            });
  }

  private String generateOrderId() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + "_"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  public String getPaymentJsUrl() {
    return paypleConfig.getPaymentJsUrl();
  }
}
