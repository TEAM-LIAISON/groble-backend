package liaison.groble.api.model.admin.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 페이지에서 정산 상세 정보에 대한 응답 DTO")
public class AdminSettlementDetailResponse {
  @Schema(
      description = "정산 ID",
      example = "1",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long settlementId;

  // 정산 시작일 (년도 + 월) - Settlement class
  @Schema(
      description = "정산 시작일 (년도 + 월)",
      example = "2023-01-01",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate settlementStartDate;

  // 정산 종료일 (년도 + 월) - Settlement class
  @Schema(
      description = "정산 종료일 (년도 + 월)",
      example = "2023-01-31",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate settlementEndDate;

  // 정산(예정)일
  @Schema(
      description = "정산(예정)일",
      example = "2023-02-01",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate scheduledSettlementDate;

  @Schema(
      description = "정산 대상 기간 동안의 총 판매 금액",
      example = "2500000.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalSalesAmount;

  @Schema(
      description = "정산 기간 동안 발생한 총 환불 금액",
      example = "150000.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalRefundAmount;

  @Schema(
      description = "정산 기간 동안 발생한 환불 건수",
      example = "2",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer refundCount;

  @Schema(
      description = "실제 적용된 총 수수료 합계 (플랫폼 + PG + VAT)",
      example = "45600.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalFee;

  @Schema(
      description = "사용자에게 표시되는 총 수수료 (플랫폼 + PG + VAT)",
      example = "48000.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalFeeDisplay;

  // 정산(예정)금액
  @Schema(
      description = "정산(예정)금액 (원화 표기)",
      example = "1000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal settlementAmount; // 정산 예정 금액 (원화 표기)

  @Schema(
      description = "사용자에게 표시되는 정산(예정)금액",
      example = "1020000.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal settlementAmountDisplay;

  // PG사 수수료(1.7%)
  @Schema(
      description = "PG사 수수료 (1.7%)",
      example = "17000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)

  @Schema(
      description = "사용자에게 표시되는 PG사 수수료",
      example = "18000.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFeeDisplay;

  @Schema(
      description = "PG 추가 수수료 환급 예정액 (실제 적용 수수료와 기준 수수료 차이 + VAT)",
      example = "13200",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFeeRefundExpected;

  // 그로블 수수료(1.5%)
  @Schema(
      description = "플랫폼 수수료 (1.5%)",
      example = "15000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)

  @Schema(
      description = "사용자에게 표시되는 플랫폼 수수료",
      example = "16500.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFeeDisplay;

  @Schema(
      description = "플랫폼에서 면제한 수수료 (이벤트/프로모션 등)",
      example = "15000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFeeForgone;

  // VAT (10%)
  @Schema(
      description = "부가세 (10%)",
      example = "1500",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal vatAmount;

  @Schema(
      description = "사용자에게 표시되는 부가세 금액",
      example = "1800.00",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal feeVatDisplay;

  @Schema(
      description = "실제 적용된 플랫폼 수수료율",
      example = "0.0150",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFeeRate;

  @Schema(
      description = "사용자에게 표시되는 플랫폼 수수료율",
      example = "0.0165",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFeeRateDisplay;

  @Schema(
      description = "정책 기준 플랫폼 수수료율",
      example = "0.0180",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFeeRateBaseline;

  @Schema(
      description = "실제 적용된 PG 수수료율",
      example = "0.0170",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFeeRate;

  @Schema(
      description = "사용자에게 표시되는 PG 수수료율",
      example = "0.0180",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFeeRateDisplay;

  @Schema(
      description = "정책 기준 PG 수수료율",
      example = "0.0195",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFeeRateBaseline;

  @Schema(
      description = "적용된 VAT율",
      example = "0.1000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal vatRate;

  @Schema(
      description = "정산 비고(페이플 응답 등 관리자 참고 메모)",
      example = "Payple SUCCESS - code: A0000, message: 승인완료",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String settlementNote;
}
