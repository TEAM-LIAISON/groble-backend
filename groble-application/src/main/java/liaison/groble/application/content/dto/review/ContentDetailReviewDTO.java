package liaison.groble.application.content.dto.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDetailReviewDTO {
  private Long reviewId;
  private Boolean isReviewManage;
  private LocalDateTime createdAt;
  private String reviewerProfileImageUrl;
  private String reviewerNickname;
  private String reviewContent;
  private String selectedOptionName;
  private BigDecimal rating;
  private String merchantUid;
  private List<ReviewReplyDTO> reviewReplies;
}
