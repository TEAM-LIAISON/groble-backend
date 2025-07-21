package liaison.groble.application.content;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.dto.FlatReviewReplyDTO;
import liaison.groble.domain.content.repository.ContentReplyCustomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentReplyReader {
  private final ContentReplyCustomRepository contentReplyCustomRepository;

  public List<FlatReviewReplyDTO> findRepliesByReviewId(Long reviewId) {
    return contentReplyCustomRepository.findRepliesByReviewId(reviewId);
  }
}
