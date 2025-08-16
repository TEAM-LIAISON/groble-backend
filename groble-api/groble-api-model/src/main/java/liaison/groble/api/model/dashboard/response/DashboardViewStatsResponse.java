package liaison.groble.api.model.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 콘텐츠/마켓 전체 조회수 응답] 대시보드 콘텐츠와 마켓의 전체 조회수 응답 반환 모델")
public class DashboardViewStatsResponse {
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
}
