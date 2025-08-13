package liaison.groble.api.model.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산관리 - 정산 상세 - 정산 상세 내역] 정산 상세 내역 응답")
public class SettlementDetailResponse {
  // 정산 시작일 (년도 + 월) - Settlement class
  @Schema(
      description = "정산 시작일 (년도 + 월)",
      example = "2023-01-01",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDate settlementStartDate;

  // 정산 종료일 (년도 + 월) - Settlement class
  @Schema(
      description = "정산 종료일 (년도 + 월)",
      example = "2023-01-31",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDate settlementEndDate;

  // 정산(예정)일
  @Schema(
      description = "정산(예정)일",
      example = "2023-02-01",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
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

  // 그로블 수수료(1.5%)
  @Schema(
      description = "플랫폼 수수료 (1.5%)",
      example = "15000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal platformFee; // 플랫폼 수수료 (1.5%)

  @Schema(
      description = "세금계산서 버튼 활성화 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean isTaxInvoiceButtonEnabled; // 세금계산서 버튼 활성화 여부

  // 세금계산서 버튼이 활성화 되더라도 간이과세자나 비사업자는 발행 불가
  @Schema(
      description = "세금계산서 발행 가능 여부 (모달 2개 관리용)",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean isTaxInvoiceIssuable; // 세금계산서 발행 가능 여부

  // 세금계산서 URL
  @Schema(
      description = "세금계산서 URL",
      example = "https://example.com/tax-invoice/12345",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String taxInvoiceUrl; // 세금계산서 URL
}
