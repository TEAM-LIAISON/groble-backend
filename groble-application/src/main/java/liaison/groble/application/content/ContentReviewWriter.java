package liaison.groble.application.content;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.repository.ContentReviewCustomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class ContentReviewWriter {
  private final ContentReviewCustomRepository contentReviewCustomRepository;

  public void updateContentReviewStatusToDeleteRequested(Long userId, Long reviewId) {
    contentReviewCustomRepository.updateContentReviewStatusToDeleteRequested(userId, reviewId);
  }

  public void deleteContentReview(Long userId, Long reviewId) {
    contentReviewCustomRepository.deleteContentReview(userId, reviewId);
  }
}
