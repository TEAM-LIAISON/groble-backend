package liaison.groble.domain.content.repository;

import liaison.groble.domain.content.entity.ContentReview;

public interface ContentReviewRepository {
  ContentReview save(ContentReview contentReview);

  boolean existsContentReview(Long userId, Long contentId);
}
