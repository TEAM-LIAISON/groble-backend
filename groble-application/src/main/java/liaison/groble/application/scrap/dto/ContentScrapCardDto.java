package liaison.groble.application.scrap.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentScrapCardDto {
  private Long contentId;
  private String contentType;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private Boolean isContentScrap;
}
