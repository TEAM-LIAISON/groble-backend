package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseTimeEntity;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_id", nullable = false)
  private Settlement settlement;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_id", nullable = false, unique = true)
  private Purchase purchase; // 구매 정보와 1:1 매핑

  // 정산 금액 정보 - DECIMAL(14,2)로 확대
  @Column(name = "sales_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal salesAmount; // 판매 금액

  @Column(name = "platform_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)

  @Column(name = "pg_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)

  @Column(name = "total_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalFee; // 총 수수료 (플랫폼 + PG)

  @Column(name = "settlement_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal settlementAmount; // 실 정산 금액 (판매금액 - 총수수료)

  // 수수료율 스냅샷 (회계 추적용)
  @Column(name = "captured_platform_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPlatformFeeRate; // 정산 시점의 플랫폼 수수료율

  @Column(name = "captured_pg_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal capturedPgFeeRate; // 정산 시점의 PG 수수료율

  // 구매 정보 스냅샷 (나중에 Purchase가 변경되어도 정산 시점의 정보 유지)
  @Column(name = "content_title", length = 255)
  private String contentTitle;

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

  // 동시성 제어를 위한 버전
  @Version private Long version;

  @Builder
  public SettlementItem(
      Settlement settlement, Purchase purchase, BigDecimal platformFeeRate, BigDecimal pgFeeRate) {
    if (settlement == null) {
      throw new IllegalArgumentException("정산 정보는 필수입니다.");
    }
    if (purchase == null) {
      throw new IllegalArgumentException("구매 정보는 필수입니다.");
    }

    // 수수료율 기본값 설정
    if (platformFeeRate == null) {
      platformFeeRate = new BigDecimal("0.0150"); // 기본 1.5%
    }
    if (pgFeeRate == null) {
      pgFeeRate = new BigDecimal("0.0170"); // 기본 1.7%
    }

    this.settlement = settlement;
    this.purchase = purchase;
    this.capturedPlatformFeeRate = platformFeeRate; // 플랫폼 수수료율 스냅샷
    this.capturedPgFeeRate = pgFeeRate; // PG 수수료율 스냅샷

    // Purchase에서 정보 복사 (null 안전 처리 및 반올림 적용)
    BigDecimal finalPrice =
        purchase.getFinalPrice() != null ? purchase.getFinalPrice() : BigDecimal.ZERO;

    this.salesAmount = finalPrice.setScale(2, RoundingMode.HALF_UP);

    // 수수료 계산
    this.platformFee = this.salesAmount.multiply(platformFeeRate).setScale(2, RoundingMode.HALF_UP);
    this.pgFee = this.salesAmount.multiply(pgFeeRate).setScale(2, RoundingMode.HALF_UP);
    this.totalFee = this.platformFee.add(this.pgFee).setScale(2, RoundingMode.HALF_UP);

    // 실 정산 금액 계산
    this.settlementAmount =
        this.salesAmount.subtract(this.totalFee).setScale(2, RoundingMode.HALF_UP);

    // 스냅샷 정보 저장
    this.contentTitle = purchase.getContent() != null ? purchase.getContent().getTitle() : null;
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

  // === 비즈니스 메서드 ===

  /** 환불 처리 */
  public void processRefund() {
    if (Boolean.TRUE.equals(this.isRefunded)) {
      throw new IllegalStateException("이미 환불 처리된 항목입니다.");
    }

    this.isRefunded = true;
    this.refundedAt = LocalDateTime.now();

    // 환불 시 정산 금액을 0으로 설정
    this.settlementAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    // Settlement의 금액 재계산 호출 (public 메서드)
    if (settlement != null) {
      settlement.recalcFromItems();
    }
  }

  /** 환불 취소 */
  public void cancelRefund() {
    if (!Boolean.TRUE.equals(this.isRefunded)) {
      throw new IllegalStateException("환불되지 않은 항목입니다.");
    }

    this.isRefunded = false;
    this.refundedAt = null;

    // 정산 금액 복원 (원래 수수료 적용)
    this.settlementAmount =
        this.salesAmount.subtract(this.totalFee).setScale(2, RoundingMode.HALF_UP);

    // Settlement의 금액 재계산 호출 (public 메서드)
    if (settlement != null) {
      settlement.recalcFromItems();
    }
  }

  /** 정산 금액 재계산 (수수료율 변경 시) */
  public void recalculateWithNewFeeRates(BigDecimal newPlatformFeeRate, BigDecimal newPgFeeRate) {
    if (Boolean.TRUE.equals(this.isRefunded)) {
      // 환불된 항목은 재계산하지 않음
      return;
    }

    this.capturedPlatformFeeRate = newPlatformFeeRate;
    this.capturedPgFeeRate = newPgFeeRate;

    // 수수료 재계산
    this.platformFee =
        this.salesAmount.multiply(newPlatformFeeRate).setScale(2, RoundingMode.HALF_UP);
    this.pgFee = this.salesAmount.multiply(newPgFeeRate).setScale(2, RoundingMode.HALF_UP);
    this.totalFee = this.platformFee.add(this.pgFee).setScale(2, RoundingMode.HALF_UP);

    // 실 정산 금액 재계산
    this.settlementAmount =
        this.salesAmount.subtract(this.totalFee).setScale(2, RoundingMode.HALF_UP);

    // Settlement의 금액 재계산
    if (settlement != null) {
      settlement.recalcFromItems();
    }
  }

  // === 연관관계 메서드 ===

  /** Settlement 설정 (양방향 관계) Settlement.addSettlementItem()에서 호출됨 */
  protected void setSettlement(Settlement settlement) {
    this.settlement = settlement;
  }

  // === 조회 메서드 ===

  /** 환불 여부 확인 (null-safe) */
  public boolean isRefundedSafe() {
    return Boolean.TRUE.equals(this.isRefunded);
  }

  /** 실제 정산 가능 금액 */
  public BigDecimal getActualSettlementAmount() {
    return isRefundedSafe() ? BigDecimal.ZERO : this.settlementAmount;
  }

  /** 총 수수료율 조회 */
  public BigDecimal getTotalFeeRate() {
    return capturedPlatformFeeRate.add(capturedPgFeeRate).setScale(4, RoundingMode.HALF_UP);
  }
}
