package liaison.groble.api.model.maker.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketLinkCheckRequest {
  @Schema(
      description = "groble.im/ 뒤에 붙는 메이커만의 마켓 링크 URL",
      example = "dongmin",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String marketLinkUrl;
}
