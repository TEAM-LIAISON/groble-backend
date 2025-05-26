package liaison.groble.domain.content.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlatDynamicContentDTO {
  private Long contentId;
  private String title;
  private String contentType;
  private String thumbnailUrl;
  private LocalDateTime updatedAt;
}
