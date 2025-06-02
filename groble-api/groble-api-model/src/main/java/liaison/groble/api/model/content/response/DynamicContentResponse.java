package liaison.groble.api.model.content.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentResponse {
  private Long contentId;
  private String title;
  private String contentType;
  private String thumbnailUrl;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;
}
