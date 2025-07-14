package liaison.groble.domain.content.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatContentReviewDetailDTO {
  private Long reviewId;
  private String contentTitle;
  private LocalDateTime createdAt;
  private String reviewerNickname;
  private String reviewContent;
  private String selectedOptionName;
  private BigDecimal rating;
}
