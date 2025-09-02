package liaison.groble.domain.content.repository;

import java.util.Optional;

import liaison.groble.domain.content.entity.ContentReview;

public interface ContentReviewRepository {
  ContentReview save(ContentReview contentReview);

  Optional<ContentReview> getContentReviewById(Long reviewId);

  boolean existsContentReview(Long userId, Long contentId);

  boolean existsContentReviewForGuest(Long guestUserId, Long contentId);
}
