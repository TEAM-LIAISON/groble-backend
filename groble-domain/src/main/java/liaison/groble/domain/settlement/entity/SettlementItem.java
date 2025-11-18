package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.purchase.entity.Purchase;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "settlement_items",
    indexes = {
      @Index(name = "idx_settlement_item_settlement", columnList = "settlement_id"),
      @Index(name = "idx_settlement_item_purchase", columnList = "purchase_id", unique = true),
      @Index(name = "idx_settlement_item_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementItem extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "settlement_id", nullable = false)
  private Settlement settlement;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_id", nullable = false, unique = true)
  private Purchase purchase; // 구매 정보와 1:1 매핑

  // 세금계산서 관계 추가
  @OneToMany(mappedBy = "settlementItem", fetch = FetchType.LAZY)
  private List<TaxInvoice> taxInvoices = new ArrayList<>();

  // 정산 금액 정보 - DECIMAL(14,2)로 확대
  @Column(name = "sales_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal salesAmount; // 판매 금액

  @Column(name = "platform_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)

  @Column(name = "platform_fee_display", nullable = false, precision = 14, scale = 2)
  private BigDecimal platformFeeDisplay = BigDecimal.ZERO; // 사용자 노출용 플랫폼 수수료

  @Column(name = "platform_fee_forgone", nullable = false, precision = 14, scale = 2)
  private BigDecimal platformFeeForgone = BigDecimal.ZERO; // 면제된 플랫폼 수수료

  @Column(name = "pg_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)

  @Column(name = "pg_fee_display", nullable = false, precision = 14, scale = 2)
  private BigDecimal pgFeeDisplay = BigDecimal.ZERO; // 사용자 노출용 PG 수수료

  @Column(name = "pg_fee_refund_expected", nullable = false, precision = 14, scale = 2)
  private BigDecimal pgFeeRefundExpected = BigDecimal.ZERO; // PG 추가 수수료 환급 예상액

  // 수수료 VAT ((플랫폼수수료 + PG수수료) * 10%) - 신규 필드
  @Column(name = "fee_vat", nullable = false, precision = 14, scale = 2)
  private BigDecimal feeVat;

  @Column(name = "fee_vat_display", nullable = false, precision = 14, scale = 2)
  private BigDecimal feeVatDisplay = BigDecimal.ZERO; // 사용자 노출용 VAT

  @Column(name = "total_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalFee; // 총 수수료 (플랫폼 + PG)

  @Column(name = "total_fee_display", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalFeeDisplay = BigDecimal.ZERO; // 사용자 노출용 총 수수료

  @Column(name = "settlement_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal settlementAmount; // 실 정산 금액 (판매금액 - 총수수료)

  @Column(name = "settlement_amount_display", nullable = false, precision = 14, scale = 2)
  private BigDecimal settlementAmountDisplay = BigDecimal.ZERO; // 사용자 노출용 정산 금액

  // 수수료율 스냅샷 (회계 추적용)
  @Column(name = "captured_platform_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPlatformFeeRate; // 정산 시점의 플랫폼 수수료율

  @Column(name = "captured_platform_fee_rate_display", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPlatformFeeRateDisplay = new BigDecimal("0.0150");

  @Column(name = "captured_platform_fee_rate_baseline", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPlatformFeeRateBaseline = new BigDecimal("0.0150");

  @Column(name = "captured_pg_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPgFeeRate; // 정산 시점의 PG 수수료율

  @Column(name = "captured_pg_fee_rate_display", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPgFeeRateDisplay = new BigDecimal("0.0170");

  @Column(name = "captured_pg_fee_rate_baseline", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPgFeeRateBaseline = new BigDecimal("0.0170");

  // VAT율 스냅샷 - 신규 필드
  @Column(name = "captured_vat_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedVatRate; // 정산 시점의 VAT율

  // 구매 정보 스냅샷 (나중에 Purchase가 변경되어도 정산 시점의 정보 유지)
  @Column(name = "content_title", length = 255)
  private String contentTitle;

  @Column(name = "content_type", length = 20)
  private String capturedContentType;

  @Column(name = "captured_payment_type", length = 20)
  private String capturedPaymentType;

  @Column(name = "option_name", length = 255)
  private String optionName;

  @Column(name = "purchaser_name", length = 255)
  private String purchaserName;

  @Column(name = "purchased_at")
  private LocalDateTime purchasedAt;

  // 환불 여부
  @Column(name = "is_refunded", nullable = false)
  private Boolean isRefunded = false;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  // 세금계산서 발급 가능 여부 (수동 관리)
  @Column(name = "tax_invoice_eligible", nullable = false)
  private Boolean taxInvoiceEligible = false;

  // 동시성 제어를 위한 버전
  @Version private Long version;

  @Builder
  public SettlementItem(
      Settlement settlement,
      Purchase purchase,
      BigDecimal platformFeeRate,
      BigDecimal platformFeeRateDisplay,
      BigDecimal platformFeeRateBaseline,
      BigDecimal pgFeeRate,
      BigDecimal pgFeeRateDisplay,
      BigDecimal pgFeeRateBaseline,
      BigDecimal vatRate) {

    if (settlement == null) {
      throw new IllegalArgumentException("정산 정보는 필수입니다.");
    }
    if (purchase == null) {
      throw new IllegalArgumentException("구매 정보는 필수입니다.");
    }

    // 수수료율 기본값 설정
    if (platformFeeRate == null) {
      platformFeeRate = new BigDecimal("0.0150"); // 적용 기본 1.5%
    }
    platformFeeRateDisplay =
        platformFeeRateDisplay != null ? platformFeeRateDisplay : platformFeeRate;
    platformFeeRateBaseline =
        platformFeeRateBaseline != null ? platformFeeRateBaseline : platformFeeRate;

    if (pgFeeRate == null) {
      pgFeeRate = new BigDecimal("0.0170"); // 적용 기본 1.7%
    }
    pgFeeRateDisplay = pgFeeRateDisplay != null ? pgFeeRateDisplay : pgFeeRate;
    pgFeeRateBaseline = pgFeeRateBaseline != null ? pgFeeRateBaseline : pgFeeRate;
    if (vatRate == null) {
      vatRate = new BigDecimal("0.1000"); // 기본 10%
    }

    this.settlement = settlement;
    this.purchase = purchase;
    this.capturedPlatformFeeRate = platformFeeRate;
    this.capturedPlatformFeeRateDisplay = platformFeeRateDisplay;
    this.capturedPlatformFeeRateBaseline = platformFeeRateBaseline;
    this.capturedPgFeeRate = pgFeeRate;
    this.capturedPgFeeRateDisplay = pgFeeRateDisplay;
    this.capturedPgFeeRateBaseline = pgFeeRateBaseline;
    this.capturedVatRate = vatRate;

    // Purchase에서 정보 복사
    BigDecimal finalPrice =
        purchase.getFinalPrice() != null ? purchase.getFinalPrice() : BigDecimal.ZERO;

    // ============ VAT 포함 수수료 계산 ============

    // 판매 금액 (이미 원 단위라고 가정)
    this.salesAmount = finalPrice.setScale(0, RoundingMode.UNNECESSARY);

    // 수수료 계산 - 원 단위로 반올림 (적용/표시/기준)
    BigDecimal platformFeeRaw = this.salesAmount.multiply(platformFeeRate);
    BigDecimal platformFeeDisplayRaw = this.salesAmount.multiply(platformFeeRateDisplay);
    BigDecimal platformFeeBaselineRaw = this.salesAmount.multiply(platformFeeRateBaseline);
    BigDecimal pgFeeRaw = this.salesAmount.multiply(pgFeeRate);
    BigDecimal pgFeeDisplayRaw = this.salesAmount.multiply(pgFeeRateDisplay);
    BigDecimal pgFeeBaselineRaw = this.salesAmount.multiply(pgFeeRateBaseline);

    this.platformFee = platformFeeRaw.setScale(0, RoundingMode.HALF_UP);
    this.platformFeeDisplay = platformFeeDisplayRaw.setScale(0, RoundingMode.HALF_UP);
    BigDecimal platformFeeBaselineRounded =
        platformFeeBaselineRaw.setScale(0, RoundingMode.HALF_UP);
    this.pgFee = pgFeeRaw.setScale(0, RoundingMode.HALF_UP);
    this.pgFeeDisplay = pgFeeDisplayRaw.setScale(0, RoundingMode.HALF_UP);
    BigDecimal pgFeeBaselineRounded = pgFeeBaselineRaw.setScale(0, RoundingMode.HALF_UP);

    BigDecimal platformForgone = platformFeeBaselineRounded.subtract(this.platformFee);
    this.platformFeeForgone = platformForgone.signum() > 0 ? platformForgone : BigDecimal.ZERO;

    // 수수료 VAT 계산 (수수료 합계의 10%) - 적용 및 표시 금액 모두 계산
    BigDecimal baseFeeApplied = this.platformFee.add(this.pgFee);
    BigDecimal baseFeeDisplay = this.platformFeeDisplay.add(this.pgFeeDisplay);
    BigDecimal feeVatRaw = baseFeeApplied.multiply(vatRate);
    BigDecimal feeVatDisplayRaw = baseFeeDisplay.multiply(vatRate);
    this.feeVat = feeVatRaw.setScale(0, RoundingMode.HALF_UP);
    this.feeVatDisplay = feeVatDisplayRaw.setScale(0, RoundingMode.HALF_UP);

    BigDecimal pgFeeExtra = this.pgFee.subtract(this.pgFeeDisplay);
    if (pgFeeExtra.signum() < 0) {
      pgFeeExtra = BigDecimal.ZERO;
    }
    BigDecimal feeVatExtra = this.feeVat.subtract(this.feeVatDisplay);
    if (feeVatExtra.signum() < 0) {
      feeVatExtra = BigDecimal.ZERO;
    }
    this.pgFeeRefundExpected = pgFeeExtra.add(feeVatExtra);

    // 총 수수료 (플랫폼 + PG + VAT)
    this.totalFee = this.platformFee.add(this.pgFee).add(this.feeVat);
    this.totalFeeDisplay = this.platformFeeDisplay.add(this.pgFeeDisplay).add(this.feeVatDisplay);

    // 실 정산 금액 계산
    this.settlementAmount = this.salesAmount.subtract(this.totalFee);
    this.settlementAmountDisplay = this.salesAmount.subtract(this.totalFeeDisplay);

    // ============ VAT 계산 완료 ============

    // 스냅샷 정보 저장
    this.contentTitle = purchase.getContent() != null ? purchase.getContent().getTitle() : null;
    // 콘텐츠 타입 캡처 (null 체크 포함)
    if (purchase.getContent() != null && purchase.getContent().getContentType() != null) {
      // ContentType enum을 문자열로 저장
      this.capturedContentType = purchase.getContent().getContentType().name();
    }
    if (purchase.getContent() != null && purchase.getContent().getPaymentType() != null) {
      this.capturedPaymentType = purchase.getContent().getPaymentType().name();
    }
    this.optionName = purchase.getSelectedOptionName();
    this.purchaserName =
        purchase.getOrder() != null && purchase.getOrder().getPurchaser() != null
            ? purchase.getOrder().getPurchaser().getName()
            : null;
    this.purchasedAt = purchase.getPurchasedAt();

    // 환불 여부 체크
    this.isRefunded = purchase.getCancelledAt() != null;
    this.refundedAt = purchase.getCancelledAt();
  }

  /** 환불 처리 - 원화 처리 환불시 정산금액을 0으로 설정 */
  public void processRefund() {
    if (Boolean.TRUE.equals(this.isRefunded)) {
      throw new IllegalStateException("이미 환불 처리된 항목입니다.");
    }

    this.isRefunded = true;
    this.refundedAt = LocalDateTime.now();

    // 환불 시 정산 금액을 0으로 설정
    this.settlementAmount = BigDecimal.ZERO;
    this.settlementAmountDisplay = BigDecimal.ZERO;

    // Settlement의 금액 재계산 호출
    if (settlement != null) {
      settlement.recalcFromItems();
    }
  }

  /** 환불 취소 환불을 취소하고 정산금액을 복원 */
  public void cancelRefund() {
    if (!Boolean.TRUE.equals(this.isRefunded)) {
      throw new IllegalStateException("환불되지 않은 항목입니다.");
    }

    this.isRefunded = false;
    this.refundedAt = null;

    // 정산 금액 복원 (원래 수수료 적용)
    this.settlementAmount = this.salesAmount.subtract(this.totalFee);
    this.settlementAmountDisplay = this.salesAmount.subtract(this.totalFeeDisplay);

    // Settlement의 금액 재계산 호출
    if (settlement != null) {
      settlement.recalcFromItems();
    }
  }

  /** 정산 금액 재계산 (수수료율 변경 시) - VAT 처리 포함 */
  public void recalculateWithNewFeeRates(
      BigDecimal newPlatformFeeRate,
      BigDecimal newPlatformFeeRateDisplay,
      BigDecimal newPlatformFeeRateBaseline,
      BigDecimal newPgFeeRate,
      BigDecimal newPgFeeRateDisplay,
      BigDecimal newPgFeeRateBaseline,
      BigDecimal newVatRate) {

    if (Boolean.TRUE.equals(this.isRefunded)) {
      return; // 환불된 항목은 재계산하지 않음
    }

    // 새로운 수수료율 저장
    if (newPlatformFeeRate == null) {
      newPlatformFeeRate = new BigDecimal("0.0150");
    }
    newPlatformFeeRateDisplay =
        newPlatformFeeRateDisplay != null ? newPlatformFeeRateDisplay : newPlatformFeeRate;
    newPlatformFeeRateBaseline =
        newPlatformFeeRateBaseline != null ? newPlatformFeeRateBaseline : newPlatformFeeRate;

    if (newPgFeeRate == null) {
      newPgFeeRate = new BigDecimal("0.0170");
    }
    newPgFeeRateDisplay = newPgFeeRateDisplay != null ? newPgFeeRateDisplay : newPgFeeRate;
    newPgFeeRateBaseline = newPgFeeRateBaseline != null ? newPgFeeRateBaseline : newPgFeeRate;

    this.capturedPlatformFeeRate = newPlatformFeeRate;
    this.capturedPlatformFeeRateDisplay = newPlatformFeeRateDisplay;
    this.capturedPlatformFeeRateBaseline = newPlatformFeeRateBaseline;
    this.capturedPgFeeRate = newPgFeeRate;
    this.capturedPgFeeRateDisplay = newPgFeeRateDisplay;
    this.capturedPgFeeRateBaseline = newPgFeeRateBaseline;
    this.capturedVatRate = newVatRate != null ? newVatRate : new BigDecimal("0.1000");

    // 수수료 재계산 - 원 단위로 반올림
    BigDecimal platformFeeRaw = this.salesAmount.multiply(newPlatformFeeRate);
    BigDecimal platformFeeDisplayRaw = this.salesAmount.multiply(newPlatformFeeRateDisplay);
    BigDecimal platformFeeBaselineRaw = this.salesAmount.multiply(newPlatformFeeRateBaseline);
    BigDecimal pgFeeRaw = this.salesAmount.multiply(newPgFeeRate);
    BigDecimal pgFeeDisplayRaw = this.salesAmount.multiply(newPgFeeRateDisplay);
    BigDecimal pgFeeBaselineRaw = this.salesAmount.multiply(newPgFeeRateBaseline);

    this.platformFee = platformFeeRaw.setScale(0, RoundingMode.HALF_UP);
    this.platformFeeDisplay = platformFeeDisplayRaw.setScale(0, RoundingMode.HALF_UP);
    BigDecimal platformFeeBaselineRounded =
        platformFeeBaselineRaw.setScale(0, RoundingMode.HALF_UP);
    this.pgFee = pgFeeRaw.setScale(0, RoundingMode.HALF_UP);
    this.pgFeeDisplay = pgFeeDisplayRaw.setScale(0, RoundingMode.HALF_UP);
    BigDecimal pgFeeBaselineRounded = pgFeeBaselineRaw.setScale(0, RoundingMode.HALF_UP);

    BigDecimal platformForgone = platformFeeBaselineRounded.subtract(this.platformFee);
    this.platformFeeForgone = platformForgone.signum() > 0 ? platformForgone : BigDecimal.ZERO;

    // 수수료 VAT 재계산
    BigDecimal baseFee = this.platformFee.add(this.pgFee);
    BigDecimal baseFeeDisplay = this.platformFeeDisplay.add(this.pgFeeDisplay);
    BigDecimal feeVatRaw = baseFee.multiply(this.capturedVatRate);
    BigDecimal feeVatDisplayRaw = baseFeeDisplay.multiply(this.capturedVatRate);
    this.feeVat = feeVatRaw.setScale(0, RoundingMode.HALF_UP);
    this.feeVatDisplay = feeVatDisplayRaw.setScale(0, RoundingMode.HALF_UP);

    BigDecimal pgFeeExtra = this.pgFee.subtract(this.pgFeeDisplay);
    if (pgFeeExtra.signum() < 0) {
      pgFeeExtra = BigDecimal.ZERO;
    }
    BigDecimal feeVatExtra = this.feeVat.subtract(this.feeVatDisplay);
    if (feeVatExtra.signum() < 0) {
      feeVatExtra = BigDecimal.ZERO;
    }
    this.pgFeeRefundExpected = pgFeeExtra.add(feeVatExtra);

    // 총 수수료 재계산
    this.totalFee = this.platformFee.add(this.pgFee).add(this.feeVat);
    this.totalFeeDisplay = this.platformFeeDisplay.add(this.pgFeeDisplay).add(this.feeVatDisplay);

    // 실 정산 금액 재계산
    this.settlementAmount = this.salesAmount.subtract(this.totalFee);
    this.settlementAmountDisplay = this.salesAmount.subtract(this.totalFeeDisplay);

    // Settlement의 금액 재계산
    if (settlement != null) {
      settlement.recalcFromItems();
    }
  }

  public boolean isSubscriptionSettlement() {
    return capturedPaymentType != null
        && ContentPaymentType.SUBSCRIPTION.name().equalsIgnoreCase(capturedPaymentType);
  }

  public void recalculateWithNewFeeRates(
      BigDecimal newPlatformFeeRate, BigDecimal newPgFeeRate, BigDecimal newVatRate) {
    recalculateWithNewFeeRates(
        newPlatformFeeRate,
        newPlatformFeeRate,
        newPlatformFeeRate,
        newPgFeeRate,
        newPgFeeRate,
        newPgFeeRate,
        newVatRate);
  }

  // === 연관관계 메서드 ===
  /** Settlement 설정 (양방향 관계) Settlement.addSettlementItem()에서 호출됨 */
  protected void setSettlement(Settlement settlement) {
    this.settlement = settlement;
  }

  // 세금계산서 발급 가능 여부 수동 설정
  public void setTaxInvoiceEligible(boolean eligible) {
    this.taxInvoiceEligible = eligible;
  }

  // === 조회 메서드 ===

  /** 환불 여부 확인 (null-safe) */
  public boolean isRefundedSafe() {
    return Boolean.TRUE.equals(this.isRefunded);
  }

  /** 실제 정산 가능 금액 환불된 경우 0, 아니면 정산금액 반환 */
  public BigDecimal getActualSettlementAmount() {
    return isRefundedSafe() ? BigDecimal.ZERO : this.settlementAmount;
  }

  /** 총 수수료율 조회 (VAT 포함 실효율) */
  public BigDecimal getTotalFeeRate() {
    BigDecimal baseFeeRate = capturedPlatformFeeRate.add(capturedPgFeeRate);
    // 실효 수수료율 = 기본수수료율 * (1 + VAT율)
    return baseFeeRate
        .multiply(BigDecimal.ONE.add(capturedVatRate))
        .setScale(4, RoundingMode.HALF_UP);
  }

  /** 기본 수수료율 조회 (VAT 제외) */
  public BigDecimal getBaseFeeRate() {
    return capturedPlatformFeeRate.add(capturedPgFeeRate).setScale(4, RoundingMode.HALF_UP);
  }

  // 가장 최근 유효한 세금계산서 조회
  public Optional<TaxInvoice> getCurrentTaxInvoice() {
    return taxInvoices.stream()
        .filter(inv -> inv.getStatus() == TaxInvoice.InvoiceStatus.ISSUED)
        .max(Comparator.comparing(TaxInvoice::getIssuedDate));
  }

  /** 이체 성공 처리 */
  public void completeSettlement() {
    // 필요하다면 상태 필드 추가 가능
    // 현재는 Settlement 레벨에서 관리
  }

  /** 이체 실패 처리 */
  public void failSettlement() {
    // 필요하다면 상태 필드 추가 가능
    // 현재는 Settlement 레벨에서 관리
  }
}
