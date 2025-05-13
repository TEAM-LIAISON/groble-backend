package liaison.groble.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlatScrapContentPreviewDTO {
  private Long contentId;
  private String contentType;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private Boolean isContentScrap;
}
