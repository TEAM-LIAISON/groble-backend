package liaison.groble.application.content.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentDto {
  private Long contentId;
  private String title;
  private String contentType;
  private String thumbnailUrl;
  private LocalDateTime updatedAt;
}
