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
  @Schema(
      description = "전체 총 판매 건수",
      example = "128",
      type = "integer",
      format = "int64",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalSalesCount;

  // N월 총 수익
  @Schema(
      description = "이번 달 총 수익 (원)",
      example = "850000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal currentMonthRevenue;

  // N월 총 판매 건수
  @Schema(
      description = "이번 달 총 판매 건수",
      example = "23",
      type = "integer",
      format = "int64",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long currentMonthSalesCount;

  // ========== 조회수 정보 - 마켓/콘텐츠 ==========
  @Schema(
      description = "마켓 전체 조회수",
      example = "15420",
      type = "integer",
      format = "int64",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalMarketViews;

  // 콘텐츠 전체 조회수
  @Schema(
      description = "콘텐츠 전체 조회수",
      example = "28350",
      type = "integer",
      format = "int64",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalContentViews;

  // ========== 조회수 정보 - 고객 ==========
  @Schema(
      description = "고객 전체 조회수 (프로필 조회수)",
      example = "8920",
      type = "integer",
      format = "int64",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalCustomerViews;

  @Schema(
      description = "고객 신규 조회수 (최근 30일 기준)",
      example = "1250",
      type = "integer",
      format = "int64",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long recentCustomerViews;
}
