package liaison.groble.api.model.settlement.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 정산 관리] 정산 관리 개요 응답")
public class SettlementOverviewResponse {

  @Schema(
      description = "메이커 인증 상태",
      example = "VERIFIED",
      type = "string",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String verificationStatus;

  // 누적 정산 금액 (전체 합산) -> 모든 월에 대한 settlementAmount의 합산
  @Schema(
      description = "누적 정산 금액 (전체 합산)",
      example = "5000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalSettlementAmount;

  // 정산 예정 금액 (이번 달 한정) -> 서버에서 자동으로 이번 달을 처리해서 반환하도록 구현
  @Schema(
      description = "정산 예정 금액 (이번 달 한정)",
      example = "1000000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal currentMonthSettlementAmount;
}
