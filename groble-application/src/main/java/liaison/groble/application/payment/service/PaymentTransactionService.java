package liaison.groble.application.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaymentAuthInfo;
import liaison.groble.application.payment.dto.PaymentCancelInfo;
import liaison.groble.application.payment.dto.PaymentCancelResult;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.completion.PaymentCompletionResult;
import liaison.groble.application.payment.validator.PaymentValidator;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.application.settlement.writer.SettlementWriter;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.user.entity.User;

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
  private final SettlementReader settlementReader;
  private final SettlementWriter settlementWriter;
  private final UserReader userReader;

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

    // =============== 정산 데이터 관리 로직 시작 ===============
    try {
      createSettlementData(purchase, payment);
      log.info("정산 데이터 생성 완료 - purchaseId: {}", purchase.getId());
    } catch (DataIntegrityViolationException e) {
      // 멱등성 보장: 이미 정산 데이터가 존재하는 경우 (재시도 등)
      log.warn("정산 데이터가 이미 존재합니다 - purchaseId: {}", purchase.getId());
    } catch (Exception e) {
      // 정산 실패는 결제 완료를 롤백하지 않음 (보상 트랜잭션 또는 배치로 처리)
      log.error("정산 데이터 생성 실패 - purchaseId: {}, error: {}", purchase.getId(), e.getMessage());
      // 실패한 정산은 별도 배치나 수동 처리를 위해 큐에 저장
      //          settlementFailureQueue.add(purchase.getId());
    }
    // =============== 정산 데이터 관리 로직 끝 ===============
    log.info(
        "결제 완료 처리 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
        order.getId(),
        payment.getId(),
        purchase.getId());

    return PaymentCompletionResult.builder()
        .orderId(order.getId())
        .merchantUid(order.getMerchantUid())
        .paymentId(payment.getId())
        .purchaseId(purchase.getId())
        .userId(order.getUser().getId())
        .contentId(purchase.getContent().getId())
        .sellerId(purchase.getContent().getUser().getId())
        .amount(payment.getPrice())
        .completedAt(purchase.getPurchasedAt())
        .sellerEmail(purchase.getContent().getUser().getEmail())
        .contentTitle(purchase.getContent().getTitle())
        .nickname(order.getUser().getNickname())
        .contentType(purchase.getContent().getContentType().name())
        .optionId(purchase.getSelectedOptionId())
        .selectedOptionName(purchase.getSelectedOptionName())
        .purchasedAt(purchase.getPurchasedAt())
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
        .pcdPayMethod(dto.getPcdPayMethod())
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

  /** 정산 데이터 생성 - 원화 반올림 로직 명확화 */
  private void createSettlementData(Purchase purchase, Payment payment) {
    Long sellerId = purchase.getContent().getUser().getId();

    // 정산 기간 계산
    LocalDate purchaseDate = purchase.getPurchasedAt().toLocalDate();
    LocalDate periodStart = purchaseDate.withDayOfMonth(1);
    LocalDate periodEnd = purchaseDate.withDayOfMonth(purchaseDate.lengthOfMonth());

    // Settlement 조회 또는 생성
    Settlement settlement = getOrCreateSettlement(sellerId, periodStart, periodEnd);

    // 멱등성 체크
    if (settlementReader.existsSettlementItemByPurchaseId(purchase.getId())) {
      log.info("정산 항목이 이미 존재합니다 - purchaseId: {}", purchase.getId());
      return;
    }

    // ========== 원화 반올림 처리 확인 로직 추가 ==========

    // 판매 금액 확인 (원 단위여야 함)
    BigDecimal salesAmount = purchase.getFinalPrice();
    if (salesAmount.scale() > 0) {
      log.warn("판매 금액에 소수점이 포함되어 있습니다. 원 단위로 변환 - amount: {}", salesAmount);
      salesAmount = salesAmount.setScale(0, RoundingMode.HALF_UP);
    }

    // 수수료 계산 미리보기 (디버그용)
    BigDecimal platformFeeRate = settlement.getPlatformFeeRate();
    BigDecimal pgFeeRate = settlement.getPgFeeRate();
    BigDecimal vatRate = settlement.getVatRate();

    // 예상 수수료 계산 (반올림 전)
    BigDecimal expectedPlatformFeeRaw = salesAmount.multiply(platformFeeRate);
    BigDecimal expectedPgFeeRaw = salesAmount.multiply(pgFeeRate);

    log.debug(
        "수수료 계산 예상 - 판매액: {}원, " + "플랫폼수수료: {}원 → {}원 ({}%), " + "PG수수료: {}원 → {}원 ({}%)",
        salesAmount.toPlainString(),
        expectedPlatformFeeRaw.toPlainString(),
        expectedPlatformFeeRaw.setScale(0, RoundingMode.HALF_UP).toPlainString(),
        platformFeeRate.multiply(new BigDecimal("100")).toPlainString(),
        expectedPgFeeRaw.toPlainString(),
        expectedPgFeeRaw.setScale(0, RoundingMode.HALF_UP).toPlainString(),
        pgFeeRate.multiply(new BigDecimal("100")).toPlainString());

    // ========== 원화 반올림 처리 확인 끝 ==========

    // SettlementItem 생성 (Builder 내부에서 자동 반올림 처리)
    SettlementItem settlementItem =
        SettlementItem.builder()
            .settlement(settlement)
            .purchase(purchase)
            .platformFeeRate(settlement.getPlatformFeeRate())
            .pgFeeRate(settlement.getPgFeeRate())
            .vatRate(settlement.getVatRate())
            .build();

    // Settlement에 항목 추가
    settlement.addSettlementItem(settlementItem);
    settlementWriter.saveSettlementItem(settlementItem);
    settlementWriter.saveSettlement(settlement);

    // ========== 반올림 결과 상세 로그 ==========
    log.info(
        "정산 생성 완료 [원화 반올림 적용] - "
            + "settlementId: {}, itemId: {}, "
            + "판매액: {}원, "
            + "플랫폼수수료: {}원 ({}%), "
            + "PG수수료: {}원 ({}%), "
            + "수수료 VAT: {}원, "
            + "총수수료: {}원, "
            + "정산액: {}원",
        settlement.getId(),
        settlementItem.getId(),
        settlementItem.getSalesAmount().toPlainString(),
        settlementItem.getPlatformFee().toPlainString(),
        platformFeeRate.multiply(new BigDecimal("100")).toPlainString(),
        settlementItem.getPgFee().toPlainString(),
        pgFeeRate.multiply(new BigDecimal("100")).toPlainString(),
        vatRate.multiply(new BigDecimal("100")).toPlainString(),
        settlementItem.getTotalFee().toPlainString(),
        settlementItem.getSettlementAmount().toPlainString());

    // 반올림으로 인한 차이 확인 (디버그 레벨)
    if (log.isDebugEnabled()) {
      BigDecimal platformFeeDiff = expectedPlatformFeeRaw.subtract(settlementItem.getPlatformFee());
      BigDecimal pgFeeDiff = expectedPgFeeRaw.subtract(settlementItem.getPgFee());

      if (platformFeeDiff.abs().compareTo(BigDecimal.ZERO) > 0
          || pgFeeDiff.abs().compareTo(BigDecimal.ZERO) > 0) {
        log.debug(
            "반올림 차이 - 플랫폼: {}원, PG: {}원",
            platformFeeDiff.toPlainString(),
            pgFeeDiff.toPlainString());
      }
    }
  }

  /** Settlement 조회 또는 생성 - 원화 처리 확인 추가 */
  private Settlement getOrCreateSettlement(
      Long sellerId, LocalDate periodStart, LocalDate periodEnd) {
    // 1차: Reader로 조회 시도
    Optional<Settlement> existingSettlement =
        settlementReader.findSettlementByUserIdAndPeriod(sellerId, periodStart, periodEnd);

    if (existingSettlement.isPresent()) {
      return existingSettlement.get();
    }

    // 2차: 없으면 Creator로 생성 시도
    try {
      User seller = userReader.getUserById(sellerId);

      // ========== 수수료율 설정 시 검증 ==========
      BigDecimal platformFeeRate = new BigDecimal("0.0150"); // 1.5%
      BigDecimal pgFeeRate = new BigDecimal("0.0170"); // 1.7%
      BigDecimal vatRate = new BigDecimal("0.1000"); // 10%

      log.info(
          "새 정산 생성 - sellerId: {}, period: {} ~ {}, " + "플랫폼수수료율: {}%, PG수수료율: {}%",
          sellerId,
          periodStart,
          periodEnd,
          platformFeeRate.multiply(new BigDecimal("100")).toPlainString(),
          pgFeeRate.multiply(new BigDecimal("100")).toPlainString());

      return settlementWriter.createSettlement(
          seller, periodStart, periodEnd, platformFeeRate, pgFeeRate, vatRate);

    } catch (DataIntegrityViolationException e) {
      // 3차: UNIQUE 제약 위반 (동시 생성) - Reader로 재조회
      log.info(
          "Settlement 동시 생성 감지, 재조회 - sellerId: {}, period: {} ~ {}",
          sellerId,
          periodStart,
          periodEnd);

      return settlementReader.getSettlementByUserIdAndPeriod(sellerId, periodStart, periodEnd);
    }
  }

  /** 결제 취소 완료 처리 - 정산 환불 처리 추가 */
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

    // ========== 정산 환불 처리 추가 ==========
    try {
      processRefundSettlement(purchase.getId());
      log.info("정산 환불 처리 완료 - purchaseId: {}", purchase.getId());
    } catch (Exception e) {
      log.error("정산 환불 처리 실패 - purchaseId: {}, error: {}", purchase.getId(), e.getMessage());
      // 환불 정산 실패는 결제 취소를 롤백하지 않음
      // 별도 배치나 수동 처리 필요
    }
    // ========== 정산 환불 처리 끝 ==========

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

  /** 환불 정산 처리 */
  private void processRefundSettlement(Long purchaseId) {
    log.info("환불 정산 처리 시작 - purchaseId: {}", purchaseId);

    // SettlementItem 조회
    SettlementItem settlementItem = settlementReader.getSettlementItemByPurchaseId(purchaseId);

    // 이미 환불 처리된 경우 체크
    if (settlementItem.isRefundedSafe()) {
      log.warn("이미 환불 처리된 정산 항목입니다 - purchaseId: {}", purchaseId);
      return;
    }

    // 환불 전 정산 정보 로그
    log.info(
        "환불 전 정산 정보 - " + "판매액: {}원, 정산액: {}원",
        settlementItem.getSalesAmount().toPlainString(),
        settlementItem.getSettlementAmount().toPlainString());

    // 환불 처리 (Settlement 합계도 자동 재계산됨)
    settlementItem.processRefund();

    // 저장
    settlementWriter.saveSettlementItem(settlementItem);
    settlementWriter.saveSettlement(settlementItem.getSettlement());

    log.info(
        "환불 정산 처리 완료 - purchaseId: {}, " + "환불금액: {}원, 환불후 정산액: {}원",
        purchaseId,
        settlementItem.getSalesAmount().toPlainString(),
        settlementItem.getSettlementAmount().toPlainString());
  }
}
