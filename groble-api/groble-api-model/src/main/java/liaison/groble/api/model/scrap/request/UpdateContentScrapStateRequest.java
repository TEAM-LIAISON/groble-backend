package liaison.groble.api.model.scrap.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateContentScrapStateRequest {
  @Schema(
      description = "스크랩 상태 변경 여부 (true : 스크랩된 상태로 변경됩니다. false : 스크랩 취소 상태로 변경됩니다.)",
      example = "true")
  private boolean changeScrapValue;
}
