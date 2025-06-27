package liaison.groble.api.model.content.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentStatusResponse {
  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @Schema(description = "변경된 콘텐츠 상태", example = "ACTIVE")
  private String status;
}
