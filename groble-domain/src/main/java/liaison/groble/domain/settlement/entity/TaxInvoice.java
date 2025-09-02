package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

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
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "tax_invoices",
    indexes = {
      @Index(name = "idx_tax_invoice_settlement", columnList = "settlement_id"),
      @Index(name = "idx_tax_invoice_settlement_item", columnList = "settlement_item_id"),
      @Index(name = "idx_tax_invoice_number", columnList = "invoice_number", unique = true),
      @Index(name = "idx_tax_invoice_issued_date", columnList = "issued_date"),
      @Index(
          name = "idx_tax_invoices_cycle_round",
          columnList = "settlement_cycle, settlement_round")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaxInvoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 관계 설정 - 다대일
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_id")
  private Settlement settlement; // 월별 발급시

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_item_id")
  private SettlementItem settlementItem; // 건별 발급시

  // 세금계산서 정보
  @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
  private String invoiceNumber; // 세금계산서 번호

  @Column(name = "invoice_url", nullable = false)
  private String invoiceUrl; // 세금계산서 URL

  @Column(name = "issued_date", nullable = false)
  private LocalDate issuedDate; // 발급일

  // 금액 정보
  @Column(name = "supply_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal supplyAmount; // 공급가액

  @Column(name = "vat_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal vatAmount; // 부가세

  @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalAmount; // 합계금액

  // 발급 타입
  @Enumerated(EnumType.STRING)
  @Column(name = "invoice_type", nullable = false, length = 20)
  private InvoiceType invoiceType;

  // 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InvoiceStatus status = InvoiceStatus.ISSUED;

  // 메모
  @Column(columnDefinition = "TEXT")
  private String note;

  // 정산 주기 정보 (월 내 몇 번째 정산인지)
  @Column(name = "settlement_round")
  private Integer settlementRound;

  // 정산 타입 정보
  @Column(name = "settlement_type", length = 20)
  private String settlementType;

  @Builder
  public TaxInvoice(
      Settlement settlement,
      SettlementItem settlementItem,
      String invoiceNumber,
      String invoiceUrl,
      BigDecimal supplyAmount,
      String businessNumber,
      String companyName,
      String representativeName) {

    // 검증: 둘 중 하나만
    if ((settlement == null) == (settlementItem == null)) {
      throw new IllegalArgumentException("Settlement 또는 SettlementItem 중 하나만 설정 가능");
    }

    this.settlement = settlement;
    this.settlementItem = settlementItem;
    this.invoiceType = settlement != null ? InvoiceType.MONTHLY : InvoiceType.PER_TRANSACTION;
    this.invoiceNumber = invoiceNumber;
    this.invoiceUrl = invoiceUrl;
    this.issuedDate = LocalDate.now();

    // 원화 단위로 반올림
    this.supplyAmount = supplyAmount.setScale(0, RoundingMode.HALF_UP);
    this.vatAmount =
        this.supplyAmount.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);
    this.totalAmount = this.supplyAmount.add(this.vatAmount);
    this.status = InvoiceStatus.ISSUED;

    // Settlement에서 정산 정보 가져오기
    if (settlement != null) {
      this.settlementRound = settlement.getSettlementRound();
      this.settlementType =
          settlement.getSettlementType() != null ? settlement.getSettlementType().name() : "LEGACY";
    }
  }

  // 세금계산서 취소
  public void cancel(String reason) {
    if (this.status != InvoiceStatus.ISSUED) {
      throw new IllegalStateException("발급된 세금계산서만 취소 가능합니다");
    }
    this.status = InvoiceStatus.CANCELLED;
    this.note = reason;
  }

  public enum InvoiceType {
    MONTHLY("월별"),
    PER_TRANSACTION("건별");

    private final String description;

    InvoiceType(String description) {
      this.description = description;
    }
  }

  public enum InvoiceStatus {
    ISSUED("발급"),
    CANCELLED("취소");

    private final String description;

    InvoiceStatus(String description) {
      this.description = description;
    }
  }
}
