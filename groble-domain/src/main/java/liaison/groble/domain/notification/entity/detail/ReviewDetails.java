package liaison.groble.domain.notification.entity.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetails {
  // 콘텐츠 ID, 리뷰 ID, 썸네일 URL
  private Long contentId;
  private Long reviewId;
  private String thumbnailUrl;
}
