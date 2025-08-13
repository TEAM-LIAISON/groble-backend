package liaison.groble.api.model.settlement.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산 관리] 개별 정산 개요 응답")
public class PerTransactionSettlementOverviewResponse {
  // 상품 제목
  @Schema(
      description = "상품 제목",
      example = "AI 툴 개발 가이드",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  // 정산 금액
  @Schema(
      description = "실 정산 금액 (원화 표기)",
      example = "9980000",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String settlementAmount;

  // 판매일
  @Schema(
      description = "판매일 (YYYY-MM-DDTHH:mm:ss)",
      example = "2023-01-15T10:30:00",
      type = "date-time",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime purchasedAt;
}
