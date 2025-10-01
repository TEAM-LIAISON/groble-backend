package liaison.groble.application.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementDetailDTO {
  // 정산 시작일 (년도 + 월) - Settlement class
  private LocalDate settlementStartDate;
  // 정산 종료일 (년도 + 월) - Settlement class
  private LocalDate settlementEndDate;
  // 정산(예정)일
  private LocalDate scheduledSettlementDate;
  // 정산(예정)금액
  private BigDecimal settlementAmount; // 정산 예정 금액 (원화 표기)
  // PG사 수수료(1.7%)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)
  // PG 추가 수수료 환급 예상액
  private BigDecimal pgFeeRefundExpected;
  // 그로블 수수료(1.5%)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)
  // 이벤트 등으로 면제한 플랫폼 수수료
  private BigDecimal platformFeeForgone;
  // VAT (10%)
  private BigDecimal vatAmount;

  // 세금계산서 버튼 활성화 값
  private Boolean isTaxInvoiceButtonEnabled; // 세금계산서 버튼 활성화 여부
  // 세금계산서 버튼이 활성화 되더라도 간이과세자나 비사업자는 발행 불가
  private Boolean isTaxInvoiceIssuable; // 세금계산서 발행 가능 여부
  // 세금계산서 URL
  private String taxInvoiceUrl; // 세금계산서 URL
}
