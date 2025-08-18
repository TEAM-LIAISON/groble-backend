package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

  // 세금계산서 관계 추가
  @OneToMany(mappedBy = "settlement", fetch = FetchType.LAZY)
  private List<TaxInvoice> taxInvoices = new ArrayList<>();

  // 세금계산서 발급 가능 여부 (수동 관리)
  @Column(name = "tax_invoice_eligible", nullable = false)
  private Boolean taxInvoiceEligible = false;

  // 정산 기간 (시작일)
  @Column(name = "settlement_start_date", nullable = false)
  private LocalDate settlementStartDate;

  // 정산 기간 (종료일)
  @Column(name = "settlement_end_date", nullable = false)
  private LocalDate settlementEndDate;

  // 필드 추가
  @Column(name = "scheduled_settlement_date", nullable = false)
  private LocalDate scheduledSettlementDate;

  // 금액 정보 - DECIMAL(14,2)로 확대
  @Column(name = "total_sales_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalSalesAmount = BigDecimal.ZERO; // 총 판매 금액

  @Column(name = "platform_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal platformFee = BigDecimal.ZERO; // 플랫폼 수수료 (1.5%)

  @Column(name = "pg_fee", nullable = false, precision = 14, scale = 2)
  private BigDecimal pgFee = BigDecimal.ZERO; // PG사 수수료 (1.7%)

  // 수수료 VAT (수수료 합계의 10%) - 신규 필드
  @Column(name = "fee_vat", nullable = false, precision = 14, scale = 2)
  private BigDecimal feeVat = BigDecimal.ZERO;

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
  // Settlement.java
  @OneToMany(
      mappedBy = "settlement",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true)
  private List<SettlementItem> settlementItems = new ArrayList<>();

  // 수수료율 (기본값: 플랫폼 1.5%, PG 1.7%)
  @Column(name = "platform_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal platformFeeRate = new BigDecimal("0.0150"); // 1.5%

  @Column(name = "pg_fee_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal pgFeeRate = new BigDecimal("0.0170"); // 1.7%

  // VAT율 - 신규 필드
  @Column(name = "vat_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal vatRate = new BigDecimal("0.1000"); // 10%

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
      BigDecimal pgFeeRate,
      BigDecimal vatRate) {
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
    this.vatRate = vatRate != null ? vatRate : new BigDecimal("0.1000"); // 기본 10%
    this.status = SettlementStatus.PENDING;
    this.scheduledSettlementDate = computeScheduledDate(this.settlementEndDate);
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

  /** 총 수수료율 계산 (플랫폼 + PG + VAT) */
  public BigDecimal getTotalFeeRate() {
    BigDecimal baseFeeRate = platformFeeRate.add(pgFeeRate);
    // 수수료에 대한 VAT까지 고려한 실효 수수료율
    // 실효율 = 기본수수료율 * (1 + VAT율)
    return baseFeeRate.multiply(BigDecimal.ONE.add(vatRate)).setScale(4, RoundingMode.HALF_UP);
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

  /**
   * 항목 스냅샷 기반으로 금액 재계산 - VAT 처리 포함 계산식: 1. 수수료 = 판매금액 * 수수료율 2. 수수료 VAT = (플랫폼수수료 + PG수수료) * 10% 3.
   * 총 차감액 = 플랫폼수수료 + PG수수료 + 수수료VAT 4. 실정산액 = 판매금액 - 총차감액
   */
  public void recalcFromItems() {
    BigDecimal gross = BigDecimal.ZERO;
    BigDecimal platformFeeSum = BigDecimal.ZERO;
    BigDecimal pgFeeSum = BigDecimal.ZERO;
    BigDecimal feeVatSum = BigDecimal.ZERO;
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
      feeVatSum = feeVatSum.add(nullSafeValue(item.getFeeVat()));
      totalFeeSum = totalFeeSum.add(nullSafeValue(item.getTotalFee()));
      net = net.add(nullSafeValue(item.getSettlementAmount()));
    }

    // 원화 처리 - 소수점 없음
    this.totalSalesAmount = gross;
    this.platformFee = platformFeeSum;
    this.pgFee = pgFeeSum;
    this.feeVat = feeVatSum;
    this.totalFee = totalFeeSum;
    this.settlementAmount = net;
    this.totalRefundAmount = refund;
    this.refundCount = refundCnt;
  }

  // 종료일 기준 다음달 1일 계산
  private static LocalDate computeScheduledDate(LocalDate endDate) {
    return endDate.plusMonths(1).withDayOfMonth(1);
  }

  // 가장 최근 유효한 세금계산서 조회
  public Optional<TaxInvoice> getCurrentTaxInvoice() {
    return taxInvoices.stream()
        .filter(inv -> inv.getStatus() == TaxInvoice.InvoiceStatus.ISSUED)
        .max(Comparator.comparing(TaxInvoice::getIssuedDate));
  }

  // 세금계산서 발급 가능 여부 수동 설정
  public void setTaxInvoiceEligible(boolean eligible) {
    this.taxInvoiceEligible = eligible;
  }
}
