package liaison.groble.application.payment.service;

//
// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;
//
// import org.json.simple.JSONObject;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
//
// import liaison.groble.application.payment.dto.PaymentCancelResponseDto;
// import liaison.groble.application.payment.dto.PaymentCompleteResponseDto;
// import liaison.groble.application.payment.dto.PaymentInfoDto;
// import liaison.groble.application.payment.dto.PaymentRequestDto;
// import liaison.groble.application.payment.dto.PaymentRequestResponseDto;
// import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
// import liaison.groble.application.payment.dto.PaypleAuthResultDto;
// import liaison.groble.application.payment.dto.PaypleLinkResponseDto;
// import liaison.groble.application.payment.dto.PayplePaymentLinkRequestDto;
// import liaison.groble.application.payment.dto.PayplePaymentResultDto;
// import liaison.groble.domain.order.entity.Order;
// import liaison.groble.domain.order.repository.OrderRepository;
// import liaison.groble.domain.payment.entity.PayplePayment;
// import liaison.groble.domain.payment.enums.PayplePaymentStatus;
// import liaison.groble.domain.payment.repository.PayplePaymentRepository;
// import liaison.groble.external.adapter.payment.PayplePayInfoRequest;
// import liaison.groble.external.adapter.payment.PaypleRefundRequest;
// import liaison.groble.external.adapter.payment.PaypleService;
// import liaison.groble.external.adapter.payment.PaypleSimplePayRequest;
// import liaison.groble.external.config.PaypleConfig;
//
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.payment.dto.PaypleAuthResultDto;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.external.adapter.payment.PaypleService;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayplePaymentService {
  private final PaypleConfig paypleConfig;
  private final PaypleService paypleService;
  private final PayplePaymentRepository payplePaymentRepository;
  private final OrderRepository orderRepository;

  // 앱카드 결제 과정에서 페이플로부터 받은 인증 값을 DB에 저장한다.
  @Transactional
  public void saveAppCardAuthResponse(PaypleAuthResultDto dto) {
    PayplePayment payplePayment =
        PayplePayment.builder()
            .pcdPayReqKey(dto.getPayReqKey())
            .pcdPayOid(dto.getPayOid())
            .pcdPayerNo(dto.getPayerNo())
            .pcdPayerName(dto.getPayerName())
            .pcdPayerHp(dto.getPayerHp())
            .pcdPayerEmail(dto.getPayerEmail())
            .pcdPayGoods(dto.getPayGoods())
            .pcdPayTotal(dto.getPayTotal())
            .pcdPayTaxTotal(dto.getPayTaxTotal())
            .pcdPayIsTax(dto.getPayIsTax())
            .pcdPayTime(dto.getPayTime())
            .pcdPayType(dto.getPayType())
            .pcdPayRst(dto.getPayRst())
            .pcdPayCode(dto.getPayCode())
            .pcdPayMsg(dto.getPayMsg())
            .status(PayplePaymentStatus.PENDING) // 명시해도 되고 builder 기본값으로 둘 수도 있음
            .build();

    payplePaymentRepository.save(payplePayment);
  }
}
//
//  public PaypleLinkResponseDto processLinkPayment(
//      PayplePaymentLinkRequestDto payplePaymentLinkRequestDto,
//      PaypleAuthResponseDto paypleAuthResponseDto) {
//    // 주문 요청자와 결제 요청자 동일 여부 검증 필요
//
//    Long orderId = payplePaymentLinkRequestDto.getOrderId();
//    Order order =
//        orderRepository
//            .findById(orderId)
//            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
//    String contentTitle = order.getOrderItems().get(0).getContent().getTitle();
//
//    Map<String, String> params = new HashMap<>();
//    if (paypleAuthResponseDto != null) {
//      params.put("PCD_AUTH_KEY", paypleAuthResponseDto.getAuthKey());
//      params.put("PCD_PAY_WORK", "LINKREG");
//      params.put("PCD_PAY_GOODS", contentTitle);
//    }
//
//    Map<String, BigDecimal> amounts = new HashMap<>();
//    amounts.put("PCD_PAY_TOTAL", order.getFinalAmount());
//
//    JSONObject linkPayResult = paypleService.payLinkCreate(params, amounts);
//
//    return PaypleLinkResponseDto.builder()
//        .paymentResult((String) linkPayResult.get("PCD_PAY_RST"))
//        .paymentLinkUrl((String) linkPayResult.get("PCD_LINK_URL"))
//        .build();
//  }
//
//  /** 결제 인증 정보 조회 */
//  // payWork
//  public PaypleAuthResponseDto getPaymentAuth(String payWork) {
//    Map<String, String> params = new HashMap<>();
//    if (payWork != null) {
//      params.put("PCD_PAY_WORK", payWork);
//    }
//
//    JSONObject authResult = paypleService.payAuth(params);
//
//    return PaypleAuthResponseDto.builder()
//        .clientKey(paypleConfig.getClientKey())
//        .authKey((String) authResult.get("AuthKey"))
//        .returnUrl((String) authResult.get("return_url"))
//        .result((String) authResult.get("result"))
//        .resultMsg((String) authResult.get("result_msg"))
//        .build();
//  }
//
//  /** 결제 요청 처리 */
//  @Transactional
//  public PaymentRequestResponseDto processPayment(
//      Long userId, PaymentRequestDto paymentRequestDto) {
//    // 주문번호 생성
//    String orderId = generateOrderId();
//
////    // Payment 엔티티 생성 및 저장
////    PayplePayment payment =
////        PayplePayment.builder()
////            .orderId(orderId)
////            .userId(userId)
////            .amount(paymentRequestDto.getAmount())
////            .payMethod(paymentRequestDto.getPayMethod())
////            .status(PayplePaymentStatus.PENDING)
////            .productName(paymentRequestDto.getProductName())
////            .build();
////
////    PayplePayment savedPayplePayment = payplePaymentRepository.save(payment);
//
//    return null;
//  }
//
//  /** 결제 완료 처리 */
//  @Transactional
//  public PaymentCompleteResponseDto completePayment(PayplePaymentResultDto payplePaymentResultDto)
// {
//    PayplePayment payment =
//        payplePaymentRepository
//            .findByOrderId(payplePaymentResultDto.getPayOid())
//            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
//
//    if ("success".equals(payplePaymentResultDto.getPayRst())) {
//      payment.complete(
//          payplePaymentResultDto.getPayerId(),
//          payplePaymentResultDto.getPayTime(),
//          payplePaymentResultDto.getPayCardName(),
//          payplePaymentResultDto.getPayCardNum());
//    } else {
//      payment.fail(payplePaymentResultDto.getPayMsg());
//    }
//
//    return PaymentCompleteResponseDto.from(payplePaymentRepository.save(payment));
//  }
//
//  /** 결제 취소 */
//  @Transactional
//  public PaymentCancelResponseDto cancelPayment(String orderId, String reason) {
//    PayplePayment payment =
//        payplePaymentRepository
//            .findByOrderId(orderId)
//            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
//
//    // 결제 취소 API 호출
//    PaypleRefundRequest refundRequest =
//        PaypleRefundRequest.builder()
//            .payOid(payment.getOrderId())
//            .payDate(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
//            .refundTotal(payment.getAmount().toString())
//            .build();
//
//    JSONObject refundResult = paypleService.payRefund(refundRequest);
//
//    if ("success".equals(refundResult.get("PCD_PAY_RST"))) {
//      payment.cancel(reason);
//      return PaymentCancelResponseDto.from(payplePaymentRepository.save(payment));
//    } else {
//      throw new RuntimeException("결제 취소 실패: " + refundResult.get("PCD_PAY_MSG"));
//    }
//  }
//

//  /** 결제 정보 조회 */
//  public PaymentInfoDto getPaymentInfo(String orderId) {
//    PayplePayment payment =
//        payplePaymentRepository
//            .findByOrderId(orderId)
//            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
//
//    // Payple API를 통한 결제 정보 조회
//    PayplePayInfoRequest request =
//        PayplePayInfoRequest.builder()
//            .payType(payment.getPayMethod().toLowerCase())
//            .payOid(payment.getOrderId())
//            .payDate(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
//            .build();
//
//    JSONObject payInfo = paypleService.payInfo(request);
//
//    return PaymentInfoDto.from(payment, payInfo);
//  }
//
//
//  private String generateOrderId() {
//    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
//        + "_"
//        + UUID.randomUUID().toString().substring(0, 8);
//  }
//
//  public String getPaymentJsUrl() {
//    return paypleConfig.getPaymentJsUrl();
//  }
//
//  /** 페이플 인증 결과 처리 */
//  @Transactional
//  public PaymentCompleteResponseDto processAuthResult(PaypleAuthResultDto authResult) {
//    log.info("페이플 인증 결과 처리 시작 - 주문번호: {}", authResult.getPayOid());
//
//    // 주문 및 결제 정보 조회
//    PayplePayment payment =
//        payplePaymentRepository
//            .findByOrderId(authResult.getPayOid())
//            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
//
//    try {
//      // 결제 수단에 따른 승인 요청 처리
//      JSONObject approvalResult;
//
//      if ("card".equals(authResult.getPayType()) && "02".equals(authResult.getCardVer())) {
//        // 앱카드 결제 승인 요청
//        approvalResult = processAppCardApproval(authResult);
//      } else if ("card".equals(authResult.getPayType())) {
//        // 일반 카드 결제 승인 요청
//        approvalResult = processCardApproval(authResult);
//      } else if ("transfer".equals(authResult.getPayType())) {
//        // 계좌이체 승인 요청
//        approvalResult = processTransferApproval(authResult);
//      } else {
//        throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + authResult.getPayType());
//      }
//
//      // 승인 결과 처리
//      if ("success".equals(approvalResult.get("PCD_PAY_RST"))) {
//        payment.complete(
//            (String) approvalResult.get("PCD_PAYER_ID"),
//            (String) approvalResult.get("PCD_PAY_TIME"),
//            (String) approvalResult.get("PCD_PAY_CARDNAME"),
//            (String) approvalResult.get("PCD_PAY_CARDNUM"));
//
//        // 영수증 URL 저장
//        if (approvalResult.get("PCD_PAY_CARDRECEIPT") != null) {
//          payment.setReceiptUrl((String) approvalResult.get("PCD_PAY_CARDRECEIPT"));
//        }
//      } else {
//        payment.fail((String) approvalResult.get("PCD_PAY_MSG"));
//      }
//
//      return PaymentCompleteResponseDto.from(payplePaymentRepository.save(payment));
//
//    } catch (Exception e) {
//      log.error("페이플 승인 요청 실패 - 주문번호: {}", authResult.getPayOid(), e);
//      payment.fail("승인 요청 실패: " + e.getMessage());
//      payplePaymentRepository.save(payment);
//      throw new RuntimeException("결제 승인 처리 중 오류가 발생했습니다.", e);
//    }
//  }
//
//  /** 앱카드 승인 요청 처리 */
//  private JSONObject processAppCardApproval(PaypleAuthResultDto authResult) {
//    Map<String, String> params = new HashMap<>();
//    params.put("PCD_CST_ID", paypleConfig.getCstId());
//    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
//    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
//    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());
//    params.put("PCD_PAYER_ID", authResult.getPayerId());
//
//    log.info("앱카드 승인 요청 - 주문번호: {}", authResult.getPayOid());
//    return paypleService.payAppCard(params);
//  }
//
//  /** 일반 카드 승인 요청 처리 */
//  private JSONObject processCardApproval(PaypleAuthResultDto authResult) {
//    Map<String, String> params = new HashMap<>();
//    params.put("PCD_CST_ID", paypleConfig.getCstId());
//    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
//    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
//    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());
//    params.put("PCD_PAYER_ID", authResult.getPayerId());
//    params.put("PCD_PAY_OID", authResult.getPayOid());
//
//    log.info("카드 승인 요청 - 주문번호: {}", authResult.getPayOid());
//    return paypleService.payConfirm(params);
//  }
//
//  /** 계좌이체 승인 요청 처리 */
//  private JSONObject processTransferApproval(PaypleAuthResultDto authResult) {
//    Map<String, String> params = new HashMap<>();
//    params.put("PCD_CST_ID", paypleConfig.getCstId());
//    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
//    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
//    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());
//    params.put("PCD_PAYER_ID", authResult.getPayerId());
//    params.put("PCD_PAY_OID", authResult.getPayOid());
//
//    log.info("계좌이체 승인 요청 - 주문번호: {}", authResult.getPayOid());
//    return paypleService.payConfirm(params);
//  }
// }
