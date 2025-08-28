package liaison.groble.api.model.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산 관리] 월별 정산 개요 응답")
public class SettlementsOverviewResponse {
  @Schema(
      description = "정산 ID",
      example = "1",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long settlementId;

  @Schema(
      description = "정산 시작일 (년도 + 월)",
      example = "2023-01-01",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate settlementStartDate;

  @Schema(
      description = "정산 종료일 (년도 + 월)",
      example = "2023-01-31",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate settlementEndDate;

  @Schema(
      description = "정산 예정일 (년도 + 월 + 일)",
      example = "2023-02-10",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate scheduledSettlementDate;

  @Schema(
      description = "콘텐츠 유형 (DIGITAL: 디지털, PHYSICAL: 실물)",
      example = "DIGITAL",
      type = "string",
      allowableValues = {"DOCUMENT", "COACHING", "LEGACY"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  @Schema(
      description = "정산(예정)금액 (원화 표기)",
      example = "1000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal settlementAmount;

  @Schema(
      description =
          "정산 상태 (PENDING: 정산 예정, PROCESSING: 정산 처리중, COMPLETED: 정산 완료, ON_HOLD: 정산 보류, CANCELLED: 정산 취소)",
      example = "COMPLETED",
      allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "ON_HOLD", "CANCELLED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String settlementStatus;
}
