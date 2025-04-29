package liaison.groble.application.content.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDetailDto {
  private Long id;
  private String contentType;
  private Long categoryId;
  private String title;
  private String sellerProfileImageUrl;
  private String sellerName;
}
