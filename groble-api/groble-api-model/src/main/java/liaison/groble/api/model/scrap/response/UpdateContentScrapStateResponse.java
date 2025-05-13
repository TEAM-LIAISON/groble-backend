package liaison.groble.api.model.scrap.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateContentScrapStateResponse {

  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @Schema(
      description = "콘텐츠 스크랩 상태 (true : 스크랩된 상태로 변경되었습니다. false : 스크랩 취소 상태로 변경되었습니다.)",
      example = "true")
  private Boolean isContentScrap;
}
