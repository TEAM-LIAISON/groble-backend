package liaison.groble.api.model.dashboard.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 메인 개요] 대시보드 메인 개요 응답")
public class DashboardOverviewResponse {
  // ========== 메이커 인증 정보 ==========
  @Schema(
      description = "메이커 인증 상태",
      example = "VERIFIED",
      type = "string",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String verificationStatus;

  // ========== 수익 정보 ==========
  @Schema(
      description = "전체 총 수익 (원)",
      example = "5250000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalRevenue;

  // 전체 총 판매 건수

  // N월 총 수익
  // N월 총 판매 건수

  // 마켓 전체 조회수
  // 콘텐츠 전체 조회수

  // 고객 전체 조회수
  // 고객 신규 조회수 (최근 30일 기준)
}
