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

  // 정산(예정)금액
  @Schema(
      description = "정산(예정)금액 (원화 표기)",
      example = "1000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal settlementAmount; // 정산 예정 금액 (원화 표기)

  // PG사 수수료(1.7%)
  @Schema(
      description = "PG사 수수료 (1.7%)",
      example = "17000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal pgFee; // PG사 수수료 (1.7%)

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
}
