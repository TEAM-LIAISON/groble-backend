package liaison.groble.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDetails {
  private Long contentId;
  private String thumbnailUrl;
  private Boolean isContentApproved;
}
