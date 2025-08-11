package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "settlements",
    indexes = {
      @Index(name = "idx_settlement_user", columnList = "user_id"),
      @Index(
          name = "idx_settlement_period",
          columnList = "settlement_start_date, settlement_end_date"),
      @Index(name = "idx_settlement_status", columnList = "status"),
      @Index(
          name = "idx_settlement_user_period",
          columnList = "user_id, settlement_start_date, settlement_end_date",
          unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 정산 대상자 (판매자)

  // 정산 기간
  @Column(name = "settlement_start_date", nullable = false)
  private LocalDate settlementStartDate;

  @Column(name = "settlement_end_date", nullable = false)
  private LocalDate settlementEndDate;

  // 금액 정보 - DECIMAL(14,2)로 확대
  @Column(name = "total_sales_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalSalesAmount = BigDecimal.ZERO; // 총 판매 금액

  @Column(name = "platform_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal platformFee = BigDecimal.ZERO; // 플랫폼 수수료 (1.5%)

  @Column(name = "pg_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal pgFee = BigDecimal.ZERO; // PG사 수수료 (1.7%)

  @Column(name = "total_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalFee = BigDecimal.ZERO; // 총 수수료 (플랫폼 + PG)

  @Column(name = "settlement_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal settlementAmount = BigDecimal.ZERO; // 실 정산 금액 (판매금액 - 총수수료)

  // 환불 집계 정보 추가
  @Column(name = "total_refund_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalRefundAmount = BigDecimal.ZERO; // 총 환불 금액

  @Column(name = "refund_count", nullable = false)
  private Integer refundCount = 0; // 환불 건수

  // 정산 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SettlementStatus status = SettlementStatus.PENDING;

  // 정산 완료 정보
  @Column(name = "settled_at")
  private LocalDateTime settledAt; // 정산 완료 일시

  @Column(name = "settlement_note", columnDefinition = "TEXT")
  private String settlementNote; // 정산 메모

  // 정산 항목들
  @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SettlementItem> settlementItems = new ArrayList<>();

  // 수수료율 (기본값: 플랫폼 1.5%, PG 1.7%)
  @Column(name = "platform_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal platformFeeRate = new BigDecimal("0.0150"); // 1.5%

  @Column(name = "pg_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal pgFeeRate = new BigDecimal("0.0170"); // 1.7%

  // 정산 은행 정보
  @Column(name = "bank_name", length = 100)
  private String bankName;

  @Column(name = "account_number", length = 100)
  private String accountNumber;

  @Column(name = "account_holder", length = 100)
  private String accountHolder;

  // 동시성 제어를 위한 버전
  @Version private Long version;

  @Builder
  public Settlement(
      User user,
      LocalDate settlementStartDate,
      LocalDate settlementEndDate,
      BigDecimal platformFeeRate,
      BigDecimal pgFeeRate) {
    // 입력 검증
    if (user == null) {
      throw new IllegalArgumentException("사용자는 필수입니다.");
    }
    if (settlementStartDate == null || settlementEndDate == null) {
      throw new IllegalArgumentException("정산 기간은 필수입니다.");
    }
    if (settlementStartDate.isAfter(settlementEndDate)) {
      throw new IllegalArgumentException("정산 시작일이 종료일보다 늦을 수 없습니다.");
    }

    this.user = user;
    this.settlementStartDate = settlementStartDate;
    this.settlementEndDate = settlementEndDate;
    this.platformFeeRate =
        platformFeeRate != null ? platformFeeRate : new BigDecimal("0.0150"); // 기본 1.5%
    this.pgFeeRate = pgFeeRate != null ? pgFeeRate : new BigDecimal("0.0170"); // 기본 1.7%
    this.status = SettlementStatus.PENDING;
  }

  // === 비즈니스 메서드 ===

  /** 정산 항목 추가 */
  public void addSettlementItem(SettlementItem item) {
    ensureModifiable();
    settlementItems.add(item);
    item.setSettlement(this);
    recalcFromItems();
  }

  /** 정산 항목 제거 */
  public void removeSettlementItem(SettlementItem item) {
    ensureModifiable();
    settlementItems.remove(item);
    item.setSettlement(null);
    recalcFromItems();
  }

  /** 항목 스냅샷 기반으로 금액 재계산 (public 메서드) 환불된 항목은 제외하고 계산 */
  public void recalcFromItems() {
    BigDecimal gross = BigDecimal.ZERO;
    BigDecimal platformFeeSum = BigDecimal.ZERO;
    BigDecimal pgFeeSum = BigDecimal.ZERO;
    BigDecimal totalFeeSum = BigDecimal.ZERO;
    BigDecimal net = BigDecimal.ZERO;
    BigDecimal refund = BigDecimal.ZERO;
    int refundCnt = 0;

    for (SettlementItem item : settlementItems) {
      if (Boolean.TRUE.equals(item.getIsRefunded())) {
        // 환불 항목
        refund = refund.add(nullSafeValue(item.getSalesAmount()));
        refundCnt++;
        continue;
      }

      // 정상 항목
      gross = gross.add(nullSafeValue(item.getSalesAmount()));
      platformFeeSum = platformFeeSum.add(nullSafeValue(item.getPlatformFee()));
      pgFeeSum = pgFeeSum.add(nullSafeValue(item.getPgFee()));
      totalFeeSum = totalFeeSum.add(nullSafeValue(item.getTotalFee()));
      net = net.add(nullSafeValue(item.getSettlementAmount()));
    }

    this.totalSalesAmount = gross.setScale(2, RoundingMode.HALF_UP);
    this.platformFee = platformFeeSum.setScale(2, RoundingMode.HALF_UP);
    this.pgFee = pgFeeSum.setScale(2, RoundingMode.HALF_UP);
    this.totalFee = totalFeeSum.setScale(2, RoundingMode.HALF_UP);
    this.settlementAmount = net.setScale(2, RoundingMode.HALF_UP);
    this.totalRefundAmount = refund.setScale(2, RoundingMode.HALF_UP);
    this.refundCount = refundCnt;
  }

  private static BigDecimal nullSafeValue(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  /** 정산 처리 시작 */
  public void startProcessing() {
    if (this.status != SettlementStatus.PENDING) {
      throw new IllegalStateException("대기 중인 정산만 처리를 시작할 수 있습니다.");
    }
    validateBankInfo();
    this.status = SettlementStatus.PROCESSING;
  }

  /** 정산 완료 처리 */
  public void complete() {
    if (this.status != SettlementStatus.PROCESSING) {
      throw new IllegalStateException("처리 중인 정산만 완료할 수 있습니다.");
    }
    this.status = SettlementStatus.COMPLETED;
    this.settledAt = LocalDateTime.now();
  }

  /** 정산 보류 처리 */
  public void hold(String reason) {
    if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.CANCELLED) {
      throw new IllegalStateException("완료되거나 취소된 정산은 보류할 수 없습니다.");
    }
    this.status = SettlementStatus.ON_HOLD;
    this.settlementNote = reason;
  }

  /** 보류에서 재개 */
  public void resumeFromHold() {
    if (this.status != SettlementStatus.ON_HOLD) {
      throw new IllegalStateException("보류 상태의 정산만 재개할 수 있습니다.");
    }
    this.status = SettlementStatus.PENDING;
    this.settlementNote = null;
  }

  /** 정산 취소 처리 */
  public void cancel(String reason) {
    if (this.status == SettlementStatus.COMPLETED) {
      throw new IllegalStateException("완료된 정산은 취소할 수 없습니다.");
    }
    this.status = SettlementStatus.CANCELLED;
    this.settlementNote = reason;
  }

  /** 수정 가능 여부 확인 */
  private void ensureModifiable() {
    if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.CANCELLED) {
      throw new IllegalStateException("완료되거나 취소된 정산은 변경할 수 없습니다.");
    }
  }

  /** 은행 정보 설정 */
  public void updateBankInfo(String bankName, String accountNumber, String accountHolder) {
    ensureModifiable();

    if (bankName == null || bankName.trim().isEmpty()) {
      throw new IllegalArgumentException("은행명은 필수입니다.");
    }
    if (accountNumber == null || accountNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("계좌번호는 필수입니다.");
    }
    if (accountHolder == null || accountHolder.trim().isEmpty()) {
      throw new IllegalArgumentException("예금주명은 필수입니다.");
    }

    this.bankName = bankName;
    this.accountNumber = accountNumber;
    this.accountHolder = accountHolder;
  }

  /** 은행 정보 검증 */
  private void validateBankInfo() {
    if (bankName == null || accountNumber == null || accountHolder == null) {
      throw new IllegalStateException("정산 처리를 위해서는 은행 정보가 필요합니다.");
    }
  }

  /** 총 수수료율 계산 (플랫폼 + PG) */
  public BigDecimal getTotalFeeRate() {
    return platformFeeRate.add(pgFeeRate).setScale(4, RoundingMode.HALF_UP);
  }

  // === Enum ===
  public enum SettlementStatus {
    PENDING("정산 예정"),
    PROCESSING("정산 처리중"),
    COMPLETED("정산 완료"),
    ON_HOLD("정산 보류"),
    CANCELLED("정산 취소");

    private final String description;

    SettlementStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
