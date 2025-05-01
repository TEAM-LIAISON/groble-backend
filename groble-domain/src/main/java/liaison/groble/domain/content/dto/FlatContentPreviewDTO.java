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
public class FlatContentPreviewDTO {
  private Long contentId;
  private LocalDateTime createdAt;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private String status;
}
