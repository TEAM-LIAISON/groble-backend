package liaison.groble.domain.content.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatReviewReplyDTO {
  private Long replyId;
  private LocalDateTime createdAt;
  private String replierNickname;
  private String replyContent;
}
