package liaison.groble.api.model.content.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentResponse {
  private Long contentId;
  private String title;
  private String contentType;
  private String thumbnailUrl;
  private LocalDateTime updatedAt;
}
