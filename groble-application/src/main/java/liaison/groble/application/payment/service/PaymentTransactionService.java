package liaison.groble.application.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaymentAuthInfo;
import liaison.groble.application.payment.dto.PaymentCancelInfo;
import liaison.groble.application.payment.dto.PaymentCancelResult;
import liaison.groble.application.payment.dto.PaymentCompletionResult;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.validator.PaymentValidator;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 관련 트랜잭션을 관리하는 서비스
 *
 * <p>모든 DB 작업은 이 서비스를 통해 트랜잭션 내에서 수행됩니다. 외부 API 호출은 이 서비스에서 수행하지 않습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {
  private final OrderReader orderReader;
  private final PurchaseReader purchaseReader;
  private final PaymentReader paymentReader;
  private final PaymentValidator paymentValidator;
  private final PayplePaymentRepository payplePaymentRepository;
  private final PaymentRepository paymentRepository;
  private final PurchaseRepository purchaseRepository;

  /**
   * 인증 정보 저장 및 검증
   *
   * @param userId 사용자 ID
   * @param authResult 인증 결과
   * @return 결제 인증 정보
   */
  @Transactional
  public PaymentAuthInfo saveAuthAndValidate(Long userId, PaypleAuthResultDTO authResult) {
    log.debug("인증 정보 저장 시작 - userId: {}, merchantUid: {}", userId, authResult.getPayOid());

    // 1. 주문 조회
    Order order = orderReader.getOrderByMerchantUid(authResult.getPayOid());

    // 2. 검증
    paymentValidator.validateOrderOwnership(order, userId);
    paymentValidator.validateOrderStatus(order, Order.OrderStatus.PENDING);
    paymentValidator.validatePaymentAmount(order.getFinalPrice(), authResult.getPayTotal());

    // 3. PayplePayment 저장
    PayplePayment payplePayment = createPayplePayment(order, authResult);
    payplePaymentRepository.save(payplePayment);

    log.info(
        "인증 정보 저장 완료 - merchantUid: {}, payplePaymentId: {}",
        authResult.getPayOid(),
        payplePayment.getId());

    return PaymentAuthInfo.builder()
        .orderId(order.getId())
        .userId(userId)
        .payplePaymentId(payplePayment.getId())
        .merchantUid(order.getMerchantUid())
        .amount(order.getFinalPrice())
        .build();
  }

  /**
   * 결제 승인 실패 처리
   *
   * @param orderId 주문 ID
   * @param errorCode 에러 코드
   * @param errorMessage 에러 메시지
   */
  @Transactional
  public void handleApprovalFailure(Long orderId, String errorCode, String errorMessage) {
    log.info("결제 승인 실패 처리 - orderId: {}, errorCode: {}", orderId, errorCode);

    Order order = orderReader.getOrderById(orderId);
    order.failOrder(String.format("결제 승인 실패 [%s]: %s", errorCode, errorMessage));

    log.info("주문 실패 처리 완료 - orderId: {}, status: {}", orderId, order.getStatus());
  }

  /**
   * 결제 완료 처리
   *
   * @param authInfo 인증 정보
   * @param approvalResult 승인 결과
   * @return 결제 완료 결과
   */
  @Transactional
  public PaymentCompletionResult completePayment(
      PaymentAuthInfo authInfo, PaypleApprovalResult approvalResult) {

    log.info("결제 완료 처리 시작 - orderId: {}", authInfo.getOrderId());

    // 1. 엔티티 조회
    Order order = orderReader.getOrderById(authInfo.getOrderId());
    PayplePayment payplePayment = paymentReader.getPayplePaymentById(authInfo.getPayplePaymentId());

    // 2. 결제 정보 일관성 검증
    paymentValidator.validatePaymentConsistency(payplePayment, approvalResult);

    // 3. PayplePayment 승인 정보 업데이트
    updatePayplePaymentApproval(payplePayment, approvalResult);

    // 4. Payment 생성 및 저장
    Payment payment = createPayment(order);

    // 5. Order 결제 완료 처리
    order.completePayment();

    // 6. Purchase 생성 및 저장
    Purchase purchase = createPurchase(order);

    log.info(
        "결제 완료 처리 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
        order.getId(),
        payment.getId(),
        purchase.getId());

    return PaymentCompletionResult.builder()
        .orderId(order.getId())
        .paymentId(payment.getId())
        .purchaseId(purchase.getId())
        .userId(order.getUser().getId())
        .contentId(purchase.getContent().getId())
        .sellerId(purchase.getContent().getUser().getId())
        .amount(payment.getPrice())
        .completedAt(purchase.getPurchasedAt())
        .sellerEmail(purchase.getContent().getUser().getEmail())
        .contentTitle(purchase.getContent().getTitle())
        .build();
  }

  /**
   * 결제 취소 가능 여부 검증
   *
   * @param userId 사용자 ID
   * @param merchantUid 주문번호
   * @return 취소 정보
   */
  @Transactional(readOnly = true)
  public PaymentCancelInfo validateCancellation(Long userId, String merchantUid) {
    log.debug("결제 취소 검증 시작 - userId: {}, merchantUid: {}", userId, merchantUid);

    // 1. 주문 조회
    Order order = orderReader.getOrderByMerchantUid(merchantUid);

    // 2. 권한 검증
    paymentValidator.validateOrderOwnership(order, userId);

    // 3. 취소 가능 상태 검증
    paymentValidator.validateCancellableStatus(order);

    // 4. 결제 정보 조회
    PayplePayment payplePayment = paymentReader.getPayplePaymentByOid(merchantUid);
    Payment payment = paymentReader.getPaymentByOrderId(order.getId());

    return PaymentCancelInfo.builder()
        .orderId(order.getId())
        .paymentId(payment.getId())
        .payplePaymentId(payplePayment.getId())
        .merchantUid(merchantUid)
        .payDate(payplePayment.getCreatedAt().toLocalDate())
        .refundAmount(new BigDecimal(payplePayment.getPcdPayTotal()))
        .refundTaxAmount(
            payplePayment.getPcdPayTaxTotal() != null
                ? new BigDecimal(payplePayment.getPcdPayTaxTotal())
                : null)
        .build();
  }

  /**
   * 결제 취소 완료 처리
   *
   * @param cancelInfo 취소 정보
   * @param reason 취소 사유
   * @return 취소 결과
   */
  @Transactional
  public PaymentCancelResult completeCancel(PaymentCancelInfo cancelInfo, String reason) {
    log.info("결제 취소 완료 처리 시작 - orderId: {}", cancelInfo.getOrderId());

    // 1. 엔티티 조회
    Order order = orderReader.getOrderById(cancelInfo.getOrderId());
    Payment payment = paymentReader.getPaymentById(cancelInfo.getPaymentId());
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    // 2. 취소 처리
    order.cancelOrder(reason);
    payment.cancel();
    purchase.cancelPayment();

    log.info(
        "결제 취소 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
        order.getId(),
        payment.getId(),
        purchase.getId());

    return PaymentCancelResult.builder()
        .orderId(order.getId())
        .paymentId(payment.getId())
        .userId(order.getUser().getId())
        .refundAmount(cancelInfo.getRefundAmount())
        .reason(reason)
        .refundedAt(LocalDateTime.now())
        .build();
  }

  /** PayplePayment 생성 */
  private PayplePayment createPayplePayment(Order order, PaypleAuthResultDTO dto) {
    return PayplePayment.builder()
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
        .pcdPayerNo(order.getUser().getId().toString())
        .pcdPayerName(dto.getPayerName())
        .pcdPayerHp(dto.getPayerHp())
        .pcdPayerEmail(dto.getPayerEmail())
        .pcdPayOid(dto.getPayOid())
        .pcdEasyPayMethod(dto.getEasyPayMethod())
        .pcdPayGoods(dto.getPayGoods())
        .pcdPayTotal(dto.getPayTotal())
        .pcdPayTaxTotal(dto.getPayTaxTotal())
        .pcdPayIsTax(dto.getPayIsTax())
        .pcdPayCardName(dto.getPayCardName())
        .pcdPayCardNum(dto.getPayCardNum())
        .pcdPayCardQuota(normalizeQuota(dto.getPayCardQuota()))
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
        .build();
  }

  /** PayplePayment 승인 정보 업데이트 */
  private void updatePayplePaymentApproval(
      PayplePayment payplePayment, PaypleApprovalResult approvalResult) {
    payplePayment.updateApprovalInfo(
        approvalResult.getPayTime(),
        approvalResult.getPayCardName(),
        approvalResult.getPayCardNum(),
        approvalResult.getPayCardTradeNum(),
        approvalResult.getPayCardAuthNo(),
        approvalResult.getPayCardReceipt());
  }

  /** Payment 생성 */
  private Payment createPayment(Order order) {
    Payment payment =
        Payment.createPgPayment(
            order, order.getFinalPrice(), Payment.PaymentMethod.CARD, order.getMerchantUid());
    return paymentRepository.save(payment);
  }

  /** Purchase 생성 */
  private Purchase createPurchase(Order order) {
    Purchase purchase = Purchase.createFromOrder(order);
    return purchaseRepository.save(purchase);
  }

  /** 할부 개월수 정규화 */
  private String normalizeQuota(String quota) {
    return (quota == null || quota.trim().isEmpty()) ? "00" : quota;
  }
}
