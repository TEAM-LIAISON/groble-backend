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
import liaison.groble.domain.settlement.enums.SettlementCycle;
import liaison.groble.domain.settlement.enums.SettlementType;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 관련 트랜잭션을 관리하는 서비스
 *
 * <p>모든 DB 작업은 이 서비스를 통해 트랜잭션 내에서 수행됩니다. 외부 API 호출은 이 서비스에서 수행하지 않습니다.
 *
 * <p>회원/비회원 모두의 결제 처리를 지원합니다.
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

    // 구매 시점 기준으로 정산 타입과 기간 결정
    LocalDate purchaseDate = purchase.getPurchasedAt().toLocalDate();

    // 판매자의 정산 타입 결정 (판매자 설정 or 콘텐츠 타입 기반)
    SettlementType settlementType = determineSettlementType(purchase);

    // 정산 기간과 회차 계산 - 새로운 calculatePeriod 메서드 활용
    Settlement.SettlementPeriod period = Settlement.calculatePeriod(settlementType, purchaseDate);

    // Settlement 조회 또는 생성 (타입과 회차 포함)
    Settlement settlement =
        getOrCreateSettlement(
            sellerId,
            period.getStartDate(),
            period.getEndDate(),
            settlementType,
            period.getRound());

    // 멱등성 체크
    if (settlementReader.existsSettlementItemByPurchaseId(purchase.getId())) {
      log.info("정산 항목이 이미 존재합니다 - purchaseId: {}", purchase.getId());
      return;
    }

    // 판매 금액 확인 (원 단위여야 함)
    BigDecimal salesAmount = purchase.getFinalPrice();
    if (salesAmount.scale() > 0) {
      log.warn("판매 금액에 소수점이 포함되어 있습니다. 원 단위로 변환 - amount: {}", salesAmount);
      salesAmount = salesAmount.setScale(0, RoundingMode.HALF_UP);
    }

    // SettlementItem 생성
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

    log.info(
        "정산 생성 완료 - "
            + "settlementId: {}, itemId: {}, "
            + "타입: {}, 주기: {}, 회차: {}, "
            + "기간: {} ~ {}, "
            + "예정일: {}, "
            + "판매액: {}원, 정산액: {}원",
        settlement.getId(),
        settlementItem.getId(),
        settlement.getSettlementType(),
        settlement.getSettlementCycle(),
        settlement.getSettlementRound(),
        settlement.getSettlementStartDate(),
        settlement.getSettlementEndDate(),
        settlement.getScheduledSettlementDate(),
        settlementItem.getSalesAmount().toPlainString(),
        settlementItem.getSettlementAmount().toPlainString());
  }

  /** Settlement 조회 또는 생성 - 타입과 회차 추가 */
  private Settlement getOrCreateSettlement(
      Long sellerId,
      LocalDate periodStart,
      LocalDate periodEnd,
      SettlementType settlementType,
      Integer settlementRound) {

    // 1차: Reader로 조회 시도
    Optional<Settlement> existingSettlement =
        settlementReader.findSettlementByUserIdAndPeriod(sellerId, periodStart, periodEnd);

    if (existingSettlement.isPresent()) {
      return existingSettlement.get();
    }

    // 2차: 없으면 Creator로 생성 시도
    try {
      User seller = userReader.getUserById(sellerId);

      BigDecimal platformFeeRate = new BigDecimal("0.0150"); // 1.5%
      BigDecimal pgFeeRate = new BigDecimal("0.0170"); // 1.7%
      BigDecimal vatRate = new BigDecimal("0.1000"); // 10%

      // 정산 주기 결정
      SettlementCycle cycle = determineSettlementCycle(settlementType);

      log.info(
          "새 정산 생성 - sellerId: {}, " + "타입: {}, 주기: {}, 회차: {}, " + "기간: {} ~ {}",
          sellerId,
          settlementType,
          cycle,
          settlementRound,
          periodStart,
          periodEnd);

      // Builder 패턴으로 생성 (새로운 필드 포함)
      Settlement settlement =
          Settlement.builder()
              .user(seller)
              .settlementStartDate(periodStart)
              .settlementEndDate(periodEnd)
              .platformFeeRate(platformFeeRate)
              .pgFeeRate(pgFeeRate)
              .vatRate(vatRate)
              .settlementType(settlementType)
              .settlementCycle(cycle)
              .settlementRound(settlementRound)
              .build();

      return settlementWriter.saveSettlement(settlement);

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

  /** 정산 타입 결정 로직 */
  private SettlementType determineSettlementType(Purchase purchase) {
    String contentType = purchase.getContent().getContentType().name();
    return switch (contentType) {
      case "DOCUMENT" -> SettlementType.DOCUMENT; // 자료형: 월 4회 정산

      case "COACHING" -> SettlementType.COACHING; // 서비스형: 월 2회 정산

      default -> SettlementType.LEGACY; // 기본: 월 1회 정산
    };
  }

  /** 정산 주기 결정 */
  private SettlementCycle determineSettlementCycle(SettlementType type) {
    switch (type) {
      case DOCUMENT:
        return SettlementCycle.WEEKLY; // 자료형: 주 단위 (월 4회)
      case COACHING:
        return SettlementCycle.BIMONTHLY; // 서비스형: 반월 단위 (월 2회)
      case LEGACY:
      default:
        return SettlementCycle.MONTHLY; // 기본: 월 단위
    }
  }

  /**
   * 비회원 인증 정보 저장 및 검증
   *
   * @param guestUserId 비회원 사용자 ID
   * @param authResult 인증 결과
   * @return 결제 인증 정보
   */
  @Transactional
  public PaymentAuthInfo saveAuthAndValidateForGuest(
      Long guestUserId, PaypleAuthResultDTO authResult) {
    log.debug(
        "비회원 인증 정보 저장 시작 - guestUserId: {}, merchantUid: {}", guestUserId, authResult.getPayOid());

    // 1. 주문 조회
    Order order = orderReader.getOrderByMerchantUid(authResult.getPayOid());

    // 2. 검증
    paymentValidator.validateOrderOwnershipForGuest(order, guestUserId);
    paymentValidator.validateOrderStatus(order, Order.OrderStatus.PENDING);
    paymentValidator.validatePaymentAmount(order.getFinalPrice(), authResult.getPayTotal());

    // 3. PayplePayment 저장
    PayplePayment payplePayment = createPayplePaymentForGuest(order, authResult);
    payplePaymentRepository.save(payplePayment);

    log.info(
        "비회원 인증 정보 저장 완료 - merchantUid: {}, payplePaymentId: {}",
        authResult.getPayOid(),
        payplePayment.getId());

    return PaymentAuthInfo.builder()
        .orderId(order.getId())
        .guestUserId(guestUserId)
        .payplePaymentId(payplePayment.getId())
        .merchantUid(order.getMerchantUid())
        .amount(order.getFinalPrice())
        .build();
  }

  /**
   * 비회원 결제 완료 처리
   *
   * @param authInfo 인증 정보
   * @param approvalResult 승인 결과
   * @return 결제 완료 결과
   */
  @Transactional
  public PaymentCompletionResult completePaymentForGuest(
      PaymentAuthInfo authInfo, PaypleApprovalResult approvalResult) {

    log.info("비회원 결제 완료 처리 시작 - orderId: {}", authInfo.getOrderId());

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
      log.info("비회원 정산 데이터 생성 완료 - purchaseId: {}", purchase.getId());
    } catch (DataIntegrityViolationException e) {
      // 멱등성 보장: 이미 정산 데이터가 존재하는 경우 (재시도 등)
      log.warn("비회원 정산 데이터가 이미 존재합니다 - purchaseId: {}", purchase.getId());
    } catch (Exception e) {
      // 정산 실패는 결제 완료를 롤백하지 않음 (보상 트랜잭션 또는 배치로 처리)
      log.error("비회원 정산 데이터 생성 실패 - purchaseId: {}, error: {}", purchase.getId(), e.getMessage());
    }
    // =============== 정산 데이터 관리 로직 끝 ===============

    log.info(
        "비회원 결제 완료 처리 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
        order.getId(),
        payment.getId(),
        purchase.getId());

    return PaymentCompletionResult.builder()
        .orderId(order.getId())
        .merchantUid(order.getMerchantUid())
        .paymentId(payment.getId())
        .purchaseId(purchase.getId())
        .guestUserId(order.getGuestUser().getId())
        .contentId(purchase.getContent().getId())
        .sellerId(purchase.getContent().getUser().getId())
        .amount(payment.getPrice())
        .completedAt(purchase.getPurchasedAt())
        .sellerEmail(purchase.getContent().getUser().getEmail())
        .contentTitle(purchase.getContent().getTitle())
        .guestUserName(order.getGuestUser().getUsername())
        .contentType(purchase.getContent().getContentType().name())
        .optionId(purchase.getSelectedOptionId())
        .selectedOptionName(purchase.getSelectedOptionName())
        .purchasedAt(purchase.getPurchasedAt())
        .build();
  }

  /**
   * 비회원 결제 취소 가능 여부 검증
   *
   * @param guestUserId 비회원 사용자 ID
   * @param merchantUid 주문번호
   * @return 취소 정보
   */
  @Transactional(readOnly = true)
  public PaymentCancelInfo validateCancellationForGuest(Long guestUserId, String merchantUid) {
    log.debug("비회원 결제 취소 검증 시작 - guestUserId: {}, merchantUid: {}", guestUserId, merchantUid);

    // 1. 주문 조회
    Order order = orderReader.getOrderByMerchantUid(merchantUid);

    // 2. 권한 검증
    paymentValidator.validateOrderOwnershipForGuest(order, guestUserId);

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

  /** 비회원 결제 취소 완료 처리 - 정산 환불 처리 추가 */
  @Transactional
  public PaymentCancelResult completeCancelForGuest(PaymentCancelInfo cancelInfo, String reason) {
    log.info("비회원 결제 취소 완료 처리 시작 - orderId: {}", cancelInfo.getOrderId());

    // 1. 엔티티 조회
    Order order = orderReader.getOrderById(cancelInfo.getOrderId());
    Payment payment = paymentReader.getPaymentById(cancelInfo.getPaymentId());
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    // 2. 취소 처리
    order.cancelOrder(reason);
    purchase.cancelPayment();

    // ========== 정산 환불 처리 추가 ==========
    try {
      processRefundSettlement(purchase.getId());
      log.info("비회원 정산 환불 처리 완료 - purchaseId: {}", purchase.getId());
    } catch (Exception e) {
      log.error("비회원 정산 환불 처리 실패 - purchaseId: {}, error: {}", purchase.getId(), e.getMessage());
      // 환불 정산 실패는 결제 취소를 롤백하지 않음
      // 별도 배치나 수동 처리 필요
    }
    // ========== 정산 환불 처리 끝 ==========

    log.info(
        "비회원 결제 취소 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
        order.getId(),
        payment.getId(),
        purchase.getId());

    return PaymentCancelResult.builder()
        .orderId(order.getId())
        .paymentId(payment.getId())
        .guestUserId(order.getGuestUser().getId())
        .refundAmount(cancelInfo.getRefundAmount())
        .reason(reason)
        .refundedAt(LocalDateTime.now())
        .build();
  }

  /** 비회원용 PayplePayment 생성 */
  private PayplePayment createPayplePaymentForGuest(Order order, PaypleAuthResultDTO dto) {
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
        .pcdPayerNo(order.getGuestUser().getId().toString())
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
}
