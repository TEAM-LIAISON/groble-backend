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
}
