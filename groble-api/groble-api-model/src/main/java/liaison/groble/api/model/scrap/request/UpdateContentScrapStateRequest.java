package liaison.groble.api.model.scrap.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateContentScrapStateRequest {
  private boolean changeScrapValue;
}
