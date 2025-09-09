package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.settlement.enums.SettlementCycle;
import liaison.groble.domain.settlement.enums.SettlementType;
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
          unique = true),
      @Index(
          name = "idx_settlement_type_cycle_round",
          columnList = "settlement_type, settlement_cycle, settlement_round")
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

  // 정산 타입 (자료형/서비스형 구분)
  @Enumerated(EnumType.STRING)
  @Column(name = "settlement_type", length = 20)
  private SettlementType settlementType;

  // 정산 주기 타입 (월1회/월2회/월4회)
  @Enumerated(EnumType.STRING)
  @Column(name = "settlement_cycle", length = 20)
  private SettlementCycle settlementCycle;

  // 정산 회차 (월 내에서 몇 번째 정산인지)
  @Column(name = "settlement_round")
  private Integer settlementRound;

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
      BigDecimal vatRate,
      SettlementType settlementType,
      SettlementCycle settlementCycle,
      Integer settlementRound) {
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

    // 신규 필드 설정 (null이면 기존 방식) - scheduledSettlementDate 계산 전에 먼저 설정
    this.settlementType = settlementType != null ? settlementType : SettlementType.LEGACY;
    this.settlementCycle = settlementCycle != null ? settlementCycle : SettlementCycle.MONTHLY;
    this.settlementRound = settlementRound != null ? settlementRound : 1;

    // 정산 예정일 계산 로직 변경 - 필드들이 초기화된 후에 계산
    this.scheduledSettlementDate =
        calculateScheduledDate(this.settlementEndDate, this.settlementType, this.settlementRound);
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

  public void approve() {
    if (this.status == SettlementStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 정산입니다: " + this.id);
    }
    if (this.status == SettlementStatus.CANCELLED) {
      throw new IllegalStateException("취소된 정산은 승인할 수 없습니다: " + this.id);
    }

    this.status = SettlementStatus.COMPLETED;
    this.settledAt = LocalDateTime.now();

    // 승인 정보를 메모에 기록
    this.settlementNote = "관리자 승인 완료";
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

  /** 콘텐츠 타입과 결제일에 따른 정산 기간 계산 기존 월 단위 계산과 호환되도록 구현 */
  public static SettlementPeriod calculatePeriod(
      SettlementType settlementType, LocalDate paymentDate) {

    if (settlementType == SettlementType.LEGACY) {
      // 기존 방식: 월 단위
      return new SettlementPeriod(
          paymentDate.withDayOfMonth(1),
          paymentDate.withDayOfMonth(paymentDate.lengthOfMonth()),
          1 // 회차
          );
    }

    int day = paymentDate.getDayOfMonth();

    if (settlementType == SettlementType.DOCUMENT) {
      // 자료형: 월 4회
      if (day >= 16 && day <= 23) {
        // 익월 1일 정산 (전월 16-23일)
        return new SettlementPeriod(
            paymentDate.withDayOfMonth(16), paymentDate.withDayOfMonth(23), 1);
      } else if (day >= 24) {
        // 익월 8일 정산 (전월 24-말일)
        return new SettlementPeriod(
            paymentDate.withDayOfMonth(24),
            paymentDate.withDayOfMonth(paymentDate.lengthOfMonth()),
            2);
      } else if (day >= 1 && day <= 7) {
        // 당월 16일 정산 (당월 1-7일)
        return new SettlementPeriod(
            paymentDate.withDayOfMonth(1), paymentDate.withDayOfMonth(7), 3);
      } else {
        // 당월 24일 정산 (당월 8-15일)
        return new SettlementPeriod(
            paymentDate.withDayOfMonth(8), paymentDate.withDayOfMonth(15), 4);
      }
    } else {
      // 서비스형: 월 2회
      if (day >= 1 && day <= 15) {
        // 익월 1일 정산 (전월 1-15일)
        return new SettlementPeriod(
            paymentDate.withDayOfMonth(1), paymentDate.withDayOfMonth(15), 1);
      } else {
        // 익월 16일 정산 (전월 16-말일)
        return new SettlementPeriod(
            paymentDate.withDayOfMonth(16),
            paymentDate.withDayOfMonth(paymentDate.lengthOfMonth()),
            2);
      }
    }
  }

  // 정산 기간 정보를 담는 내부 클래스
  @Getter
  public static class SettlementPeriod {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer round;

    public SettlementPeriod(LocalDate startDate, LocalDate endDate, Integer round) {
      this.startDate = startDate;
      this.endDate = endDate;
      this.round = round;
    }
  }

  /** 정산 예정일 계산 - 타입별로 다르게 */
  private static LocalDate calculateScheduledDate(
      LocalDate endDate, SettlementType type, Integer round) {

    if (type == SettlementType.LEGACY) {
      // 기존: 익월 1일
      return endDate.plusMonths(1).withDayOfMonth(1);
    }

    LocalDate nextMonth = endDate.plusMonths(1);

    if (type == SettlementType.DOCUMENT) {
      // 자료형: 1일, 8일, 16일, 24일
      return switch (round) {
        case 1 -> nextMonth.withDayOfMonth(1);
        case 2 -> nextMonth.withDayOfMonth(8);
        case 3 -> endDate.withDayOfMonth(16); // 당월
        case 4 -> endDate.withDayOfMonth(24); // 당월
        default -> nextMonth.withDayOfMonth(1);
      };
    } else {
      // 서비스형: 1일, 16일
      return round == 1 ? nextMonth.withDayOfMonth(1) : nextMonth.withDayOfMonth(16);
    }
  }
}
