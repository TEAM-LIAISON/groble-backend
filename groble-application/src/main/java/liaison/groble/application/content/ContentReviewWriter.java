package liaison.groble.application.content;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.repository.ContentReviewCustomRepository;
import liaison.groble.domain.content.repository.ContentReviewRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class ContentReviewWriter {
  private final ContentReviewCustomRepository contentReviewCustomRepository;
  private final ContentReviewRepository contentReviewRepository;

  public ContentReview save(ContentReview contentReview) {
    return contentReviewRepository.save(contentReview);
  }

  public void updateContentReviewStatusToDeleteRequested(Long userId, Long reviewId) {
    contentReviewCustomRepository.updateContentReviewStatusToDeleteRequested(userId, reviewId);
  }

  public void deleteContentReview(Long userId, Long reviewId) {
    contentReviewCustomRepository.deleteContentReview(userId, reviewId);
  }
}
