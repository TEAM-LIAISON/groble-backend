package liaison.groble.api.model.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 개별 콘텐츠의 전체 조회수 응답] 개별 콘텐츠의 전체 조회수를 내림차순 형태로 응답하는 모델")
public class ContentTotalViewStatsResponse {
  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  @Schema(
      description = "콘텐츠 제목",
      example = "자바 프로그래밍 코칭",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  @Schema(
      description = "해당 콘텐츠의 전체 조회수",
      example = "1500",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalViews;
}
