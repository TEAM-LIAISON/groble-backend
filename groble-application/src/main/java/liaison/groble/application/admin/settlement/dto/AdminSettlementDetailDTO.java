package liaison.groble.application.admin.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSettlementDetailDTO {
  private Long settlementId;
  // 정산 시작일 (년도 + 월) - Settlement class
  private LocalDate settlementStartDate;
  // 정산 종료일 (년도 + 월) - Settlement class
  private LocalDate settlementEndDate;
  // 정산(예정)일
  private LocalDate scheduledSettlementDate;
  // 정산 금액 집계
  private BigDecimal totalSalesAmount;
  private BigDecimal totalRefundAmount;
  private Integer refundCount;
  private BigDecimal totalFee;
  private BigDecimal totalFeeDisplay;
  // 정산(예정)금액
  private BigDecimal settlementAmount; // 정산 예정 금액 (원화 표기)
  private BigDecimal settlementAmountDisplay;
  // PG사 수수료(1.7%)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)
  private BigDecimal pgFeeDisplay;
  // PG 추가 수수료로 환급 예정 금액
  private BigDecimal pgFeeRefundExpected;
  // 그로블 수수료(1.5%)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)
  private BigDecimal platformFeeDisplay;
  // 플랫폼에서 면제한 수수료 (이벤트 등)
  private BigDecimal platformFeeForgone;
  // VAT (10%)
  private BigDecimal vatAmount;
  private BigDecimal feeVatDisplay;
  // 수수료율 정보
  private BigDecimal platformFeeRate;
  private BigDecimal platformFeeRateDisplay;
  private BigDecimal platformFeeRateBaseline;
  private BigDecimal pgFeeRate;
  private BigDecimal pgFeeRateDisplay;
  private BigDecimal pgFeeRateBaseline;
  private BigDecimal vatRate;
}
