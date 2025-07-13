package liaison.groble.application.content.dto.review;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReplyDTO {
  private Long replyId;
  private LocalDateTime createdAt;
  private String replierNickname;
  private String replyContent;
}
