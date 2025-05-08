package liaison.groble.application.scrap.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentScrapDto {
  private Long contentId;
  private Boolean isContentScrap;
}
