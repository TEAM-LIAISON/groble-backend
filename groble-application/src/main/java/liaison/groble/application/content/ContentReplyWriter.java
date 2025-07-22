package liaison.groble.application.content;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.entity.ContentReply;
import liaison.groble.domain.content.repository.ContentReplyCustomRepository;
import liaison.groble.domain.content.repository.ContentReplyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class ContentReplyWriter {
  private final ContentReplyCustomRepository contentReplyCustomRepository;
  private final ContentReplyRepository contentReplyRepository;

  public ContentReply save(ContentReply contentReply) {
    return contentReplyRepository.save(contentReply);
  }

  public void updateReply(Long userId, Long reviewId, Long replyId, String replyContent) {
    contentReplyCustomRepository.updateReply(userId, reviewId, replyId, replyContent);
  }

  public void deleteReply(Long userId, Long reviewId, Long replyId) {
    contentReplyCustomRepository.deleteReply(userId, reviewId, replyId);
  }
}
