package liaison.groble.application.payment.service;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaypleAuthResultDto;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.external.adapter.payment.PaypleRefundRequest;
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
  private final OrderRepository orderRepository;
  private final PurchaseRepository purchaseRepository;

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
            .pcdPayerNo(
                order.getUser() != null ? order.getUser().getId().toString() : dto.getPayerNo())
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
    log.info("앱카드 인증 정보 저장 완료 - 주문번호: {}", dto.getPayOid());
  }

  /** 앱카드 승인 요청 처리 - 페이플에 승인 요청 - 승인 성공 시 Order, Payment, Purchase 생성/업데이트 - 쿠폰 사용 처리 */
  @Transactional
  public JSONObject processAppCardApproval(PaypleAuthResultDto authResult) {
    // 1. 주문 조회
    Order order = orderReader.getOrderByMerchantUid(authResult.getPayOid());

    // 2. 페이플 승인 요청
    Map<String, String> params = new HashMap<>();
    params.put("PCD_CST_ID", paypleConfig.getCstId());
    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());

    log.info("앱카드 승인 요청 - 주문번호: {}", authResult.getPayOid());
    JSONObject approvalResult = paypleService.payAppCard(params);

    // 3. 승인 결과 처리
    String payRst = (String) approvalResult.get("PCD_PAY_RST");

    // 기존 PayplePayment 조회
    PayplePayment payplePayment =
        payplePaymentRepository
            .findByPcdPayOid(authResult.getPayOid())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

    if ("success".equalsIgnoreCase(payRst)) {
      // 4. 결제 정보 검증
      validatePaymentConsistency(payplePayment, approvalResult);

      // 5. PayplePayment 상태 업데이트
      payplePayment.updateStatus(PayplePaymentStatus.COMPLETED);
      payplePayment.updateApprovalInfo(
          (String) approvalResult.get("PCD_PAY_TIME"),
          (String) approvalResult.get("PCD_PAY_CARDTRADENUM"),
          (String) approvalResult.get("PCD_PAY_CARDAUTHNO"),
          (String) approvalResult.get("PCD_CARD_RECEIPT"));
      payplePaymentRepository.save(payplePayment);

      // 6. Payment 엔티티 생성 (필요시 주석 해제)
      /*
      Payment payment = Payment.builder()
          .order(order)
          .cancelReason(null)
          .build();

      order.setPayment(payment);
      paymentRepository.save(payment);
      */

      // 7. Order 상태 업데이트 (결제 완료 + 쿠폰 사용 처리)
      order.completePayment();
      orderRepository.save(order);

      // 8. Purchase 생성
      Purchase purchase = Purchase.createFromOrder(order);
      purchase.complete(); // PENDING → COMPLETED
      purchaseRepository.save(purchase);

      log.info("앱카드 결제 승인 성공 - 주문번호: {}, 구매ID: {}", authResult.getPayOid(), purchase.getId());

    } else {
      // 승인 실패 처리
      String errorMsg = (String) approvalResult.get("PCD_PAY_MSG");

      // PayplePayment 상태 업데이트
      payplePayment.updateStatus(PayplePaymentStatus.FAILED);
      payplePaymentRepository.save(payplePayment);

      // Order 실패 처리 (쿠폰 사용 취소 포함)
      order.failOrder("결제 승인 실패: " + errorMsg);
      orderRepository.save(order);

      log.error("앱카드 결제 승인 실패 - 주문번호: {}, 메시지: {}", authResult.getPayOid(), errorMsg);
    }

    return approvalResult;
  }

  /** 결제 정보 일치 검증 - 주문번호, 금액, 상품명, 구매자 정보 등 검증 */
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

    // 구매자 정보 검증 (null 체크 포함)
    validateIfNotNull(
        payment.getPcdPayerNo(), (String) approvalResult.get("PCD_PAYER_NO"), "구매자번호");
    validateIfNotNull(
        payment.getPcdPayerName(), (String) approvalResult.get("PCD_PAYER_NAME"), "구매자명");
    validateIfNotNull(
        payment.getPcdPayerHp(), (String) approvalResult.get("PCD_PAYER_HP"), "구매자 연락처");
    validateIfNotNull(
        payment.getPcdPayIsTax(), (String) approvalResult.get("PCD_PAY_ISTAX"), "과세여부");

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
    if (payment.getPcdPayCardQuota() != null) {
      String approvedCardQuota = (String) approvalResult.get("PCD_PAY_CARDQUOTA");
      if (approvedCardQuota != null && !payment.getPcdPayCardQuota().equals(approvedCardQuota)) {
        throw new IllegalStateException(
            String.format(
                "할부개월수 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayCardQuota(), approvedCardQuota));
      }
    }

    log.info("결제 정보 검증 완료 - 주문번호: {}", payment.getPcdPayOid());
  }

  /** null이 아닌 경우에만 검증 */
  private void validateIfNotNull(String dbValue, String approvalValue, String fieldName) {
    if (dbValue != null && !dbValue.equals(approvalValue)) {
      throw new IllegalStateException(
          String.format("%s 불일치 - DB: %s, 승인결과: %s", fieldName, dbValue, approvalValue));
    }
  }

  /** 결제 취소 처리 - 페이플에 취소 요청 - 성공 시 관련 엔티티들 상태 업데이트 */
  @Transactional
  public void cancelPayment(String orderId, String reason) {
    // 1. 주문 조회
    Order order =
        orderRepository
            .findByMerchantUid(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

    // 2. PayplePayment 조회
    PayplePayment payplePayment =
        payplePaymentRepository
            .findByPcdPayOid(orderId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + orderId));

    // 3. 페이플 환불 요청 객체 생성
    PaypleRefundRequest refundRequest =
        PaypleRefundRequest.builder()
            .payOid(orderId)
            .payDate(payplePayment.getCreatedAt().toLocalDate().toString().replace("-", ""))
            .refundTotal(payplePayment.getPcdPayTotal())
            .refundTaxtotal(payplePayment.getPcdPayTaxTotal()) // 복합과세인 경우
            .build();

    // 4. 페이플 환불 API 호출
    JSONObject refundResult = paypleService.payRefund(refundRequest);

    if ("success".equals(refundResult.get("PCD_PAY_RST"))) {
      // 5. PayplePayment 상태 업데이트
      payplePayment.updateStatus(PayplePaymentStatus.CANCELLED);
      payplePaymentRepository.save(payplePayment);

      // 6. Order 취소 처리 (쿠폰 사용 취소 포함)
      order.cancelOrder(reason);
      orderRepository.save(order);

      // 7. Purchase 취소 처리
      Purchase purchase =
          purchaseRepository
              .findByOrder(order)
              .orElseThrow(() -> new IllegalArgumentException("구매 정보를 찾을 수 없습니다."));
      purchase.cancel(reason);
      purchaseRepository.save(purchase);

      log.info("결제 취소 완료 - 주문번호: {}, 환불금액: {}", orderId, refundRequest.getRefundTotal());
    } else {
      String errorMsg = (String) refundResult.get("PCD_PAY_MSG");
      log.error("결제 취소 실패 - 주문번호: {}, 에러: {}", orderId, errorMsg);
      throw new RuntimeException("결제 취소 실패: " + errorMsg);
    }
  }
}
