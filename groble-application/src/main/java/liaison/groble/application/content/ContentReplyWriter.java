package liaison.groble.application.content;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.repository.ContentReplyCustomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class ContentReplyWriter {
  private final ContentReplyCustomRepository contentReplyCustomRepository;

  public void addReply(Long userId, Long reviewId, String replyContent) {
    contentReplyCustomRepository.addReply(userId, reviewId, replyContent);
  }

  public void updateReply(Long userId, Long reviewId, Long replyId, String replyContent) {
    contentReplyCustomRepository.updateReply(userId, reviewId, replyId, replyContent);
  }

  public void deleteReply(Long userId, Long reviewId, Long replyId) {
    contentReplyCustomRepository.deleteReply(userId, reviewId, replyId);
  }
}
