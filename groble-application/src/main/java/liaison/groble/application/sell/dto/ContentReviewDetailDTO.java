package liaison.groble.application.sell.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.application.content.dto.review.ReviewReplyDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentReviewDetailDTO {
  private Long reviewId;
  private String contentTitle;
  private LocalDateTime createdAt;
  private String reviewerNickname;
  private String reviewContent;
  private String selectedOptionName;
  private BigDecimal rating;
  private List<ReviewReplyDTO> reviewReplies;
}
