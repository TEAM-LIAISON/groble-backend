package liaison.groble.application.content.dto.review;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentReviewDTO {
  private BigDecimal averageRating;
  private Long totalReviewCount;
  private List<ContentDetailReviewDTO> reviews;
}
