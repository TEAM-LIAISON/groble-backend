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
public class FlatContentReviewReplyDTO {
  // Review(리뷰) 정보
  private Long reviewId;
  private Long reviewerId;
  private LocalDateTime reviewCreatedAt;
  private String reviewerProfileImageUrl;
  private String reviewerNickname;
  private String reviewContent;
  private String selectedOptionName;
  private BigDecimal rating;

  // Reply(답글) 정보
  private Long replyId;
  private LocalDateTime replyCreatedAt;
  private String replierNickname;
  private String replyContent;
}
