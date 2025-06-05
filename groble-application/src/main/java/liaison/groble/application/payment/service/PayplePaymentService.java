package liaison.groble.application.payment.service;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaypleAuthResultDto;
import liaison.groble.domain.order.entity.Order;
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
  private final PayplePaymentRepository payplePaymentRepository;
  private final PaypleService paypleService;
  private final PaypleConfig paypleConfig;
  private final OrderReader orderReader;

  /**
   * 앱카드 결제 인증 결과 저장
   *
   * <p>- 페이플로부터 받은 인증 값을 DB에 저장
   *
   * <p>- Order와 연결하여 저장
   */
  @Transactional
  public void saveAppCardAuthResponse(PaypleAuthResultDto dto) {
    // 1. 주문 조회 및 검증
    Order order = orderReader.getOrderByMerchantUid(dto.getPayOid());

    // 주문 상태 검증
    if (order.getStatus() != Order.OrderStatus.PENDING) {
      throw new IllegalStateException("결제 대기 상태의 주문만 처리할 수 있습니다.");
    }

    // 금액 검증
    if (!order.getFinalPrice().toString().equals(dto.getPayTotal())) {
      throw new IllegalStateException(
          String.format("결제 금액 불일치 - 주문: %s, 결제: %s", order.getFinalPrice(), dto.getPayTotal()));
    }

    PayplePayment payplePayment =
        PayplePayment.builder()
            .pcdPayRst(dto.getPayRst())
            .pcdPayCode(dto.getPayCode())
            .pcdPayMsg(dto.getPayMsg())
            .pcdPayType(dto.getPayType())
            .pcdPayCardVer(dto.getCardVer())
            .pcdPayWork(dto.getPayWork())
            .pcdPayAuthKey(dto.getAuthKey())
            .pcdPayReqKey(dto.getPayReqKey())
            .pcdPayHost(dto.getPayHost())
            .pcdPayCofUrl(dto.getPayCofUrl())
            .pcdPayerNo(dto.getPayerNo())
            .pcdPayerName(dto.getPayerName())
            .pcdPayerHp(dto.getPayerHp())
            .pcdPayerEmail(dto.getPayerEmail())
            .pcdPayOid(dto.getPayOid())
            .pcdPayGoods(dto.getPayGoods())
            .pcdPayTotal(dto.getPayTotal())
            .pcdPayTaxTotal(dto.getPayTaxTotal())
            .pcdPayIsTax(dto.getPayIsTax())
            .pcdPayCardName(dto.getPayCardName())
            .pcdPayCardNum(dto.getPayCardNum())
            .pcdPayCardQuota(dto.getPayCardQuota())
            .pcdPayCardTradeNum(dto.getPayCardTradeNum())
            .pcdPayCardAuthNo(dto.getPayCardAuthNo())
            .pcdPayCardReceipt(dto.getPayCardReceipt())
            .pcdPayTime(dto.getPayTime())
            .pcdRegulerFlag(dto.getRegulerFlag())
            .pcdPayYear(dto.getPayYear())
            .pcdPayMonth(dto.getPayMonth())
            .pcdSimpleFlag(dto.getSimpleFlag())
            .pcdRstUrl(dto.getRstUrl())
            .pcdUserDefine1(dto.getUserDefine1())
            .pcdUserDefine2(dto.getUserDefine2())
            .status(PayplePaymentStatus.PENDING)
            .build();

    payplePaymentRepository.save(payplePayment);
  }

  /** 앱카드 승인 요청 처리 */
  @Transactional
  public JSONObject processAppCardApproval(PaypleAuthResultDto authResult) {
    Map<String, String> params = new HashMap<>();
    params.put("PCD_CST_ID", paypleConfig.getCstId());
    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());

    log.info("앱카드 승인 요청 - 주문번호: {}", authResult.getPayOid());
    JSONObject approvalResult = paypleService.payAppCard(params);

    // 승인 결과에 따라 DB 상태 업데이트
    String payRst = (String) approvalResult.get("PCD_PAY_RST");

    // 기존 결제 정보 조회
    PayplePayment payment =
        payplePaymentRepository
            .findByPcdPayOid(authResult.getPayOid())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

    if ("success".equalsIgnoreCase(payRst)) {
      // 결제 정보 검증
      validatePaymentConsistency(payment, approvalResult);

      // 승인 성공 시 상태 업데이트
      payment.updateStatus(PayplePaymentStatus.COMPLETED);

      // 승인 정보 업데이트
      payment.updateApprovalInfo(
          (String) approvalResult.get("PCD_PAY_TIME"),
          (String) approvalResult.get("PCD_PAY_CARDTRADENUM"),
          (String) approvalResult.get("PCD_PAY_CARDAUTHNO"),
          (String) approvalResult.get("PCD_CARD_RECEIPT"));

      payplePaymentRepository.save(payment);
      log.info("앱카드 결제 승인 성공 - 주문번호: {}", authResult.getPayOid());
    } else {
      // 승인 실패 시 상태 업데이트
      payment.updateStatus(PayplePaymentStatus.FAILED);
      payplePaymentRepository.save(payment);
      log.error(
          "앱카드 결제 승인 실패 - 주문번호: {}, 메시지: {}",
          authResult.getPayOid(),
          approvalResult.get("PCD_PAY_MSG"));
    }

    return approvalResult;
  }

  /** 결제 정보 일치 검증 */
  private void validatePaymentConsistency(PayplePayment payment, JSONObject approvalResult) {
    // 주문번호 검증
    String approvedOid = (String) approvalResult.get("PCD_PAY_OID");
    if (!payment.getPcdPayOid().equals(approvedOid)) {
      throw new IllegalStateException(
          String.format("주문번호 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayOid(), approvedOid));
    }

    // 결제 금액 검증
    String approvedTotal = (String) approvalResult.get("PCD_PAY_TOTAL");
    if (!payment.getPcdPayTotal().equals(approvedTotal)) {
      throw new IllegalStateException(
          String.format("결제금액 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayTotal(), approvedTotal));
    }

    // 상품명 검증
    String approvedGoods = (String) approvalResult.get("PCD_PAY_GOODS");
    if (!payment.getPcdPayGoods().equals(approvedGoods)) {
      throw new IllegalStateException(
          String.format("상품명 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayGoods(), approvedGoods));
    }

    // 구매자 정보 검증
    String approvedPayerNo = (String) approvalResult.get("PCD_PAYER_NO");
    if (payment.getPcdPayerNo() != null && !payment.getPcdPayerNo().equals(approvedPayerNo)) {
      throw new IllegalStateException(
          String.format("구매자번호 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayerNo(), approvedPayerNo));
    }

    // 구매자명 검증
    String approvedPayerName = (String) approvalResult.get("PCD_PAYER_NAME");
    if (payment.getPcdPayerName() != null && !payment.getPcdPayerName().equals(approvedPayerName)) {
      throw new IllegalStateException(
          String.format(
              "구매자명 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayerName(), approvedPayerName));
    }

    // 구매자 연락처 검증
    String approvedPayerHp = (String) approvalResult.get("PCD_PAYER_HP");
    if (payment.getPcdPayerHp() != null && !payment.getPcdPayerHp().equals(approvedPayerHp)) {
      throw new IllegalStateException(
          String.format(
              "구매자 연락처 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayerHp(), approvedPayerHp));
    }

    // 과세 여부 검증
    String approvedIsTax = (String) approvalResult.get("PCD_PAY_ISTAX");
    if (payment.getPcdPayIsTax() != null && !payment.getPcdPayIsTax().equals(approvedIsTax)) {
      throw new IllegalStateException(
          String.format("과세여부 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayIsTax(), approvedIsTax));
    }

    // 복합과세 부가세 검증 (복합과세인 경우만)
    if (payment.getPcdPayTaxTotal() != null) {
      String approvedTaxTotal = (String) approvalResult.get("PCD_PAY_TAXTOTAL");
      if (!payment.getPcdPayTaxTotal().equals(approvedTaxTotal)) {
        throw new IllegalStateException(
            String.format(
                "복합과세 부가세 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayTaxTotal(), approvedTaxTotal));
      }
    }

    // 할부개월수 검증 (카드결제인 경우)
    String approvedCardQuota = (String) approvalResult.get("PCD_PAY_CARDQUOTA");
    if (payment.getPcdPayCardQuota() != null
        && approvedCardQuota != null
        && !payment.getPcdPayCardQuota().equals(approvedCardQuota)) {
      throw new IllegalStateException(
          String.format(
              "할부개월수 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayCardQuota(), approvedCardQuota));
    }

    log.info("결제 정보 검증 완료 - 주문번호: {}", payment.getPcdPayOid());
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
