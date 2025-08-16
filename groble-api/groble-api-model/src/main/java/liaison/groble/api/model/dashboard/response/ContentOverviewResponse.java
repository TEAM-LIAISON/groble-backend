package liaison.groble.api.model.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 콘텐츠 개요] 콘텐츠 개요 응답")
public class ContentOverviewResponse {

  @Schema(
      description = "판매하고 있는 전체 콘텐츠 개수",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalContentsCount;

  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  @Schema(
      description = "내가 판매하고 있는 콘텐츠 제목",
      example = "자바 프로그래밍 입문",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;
}
