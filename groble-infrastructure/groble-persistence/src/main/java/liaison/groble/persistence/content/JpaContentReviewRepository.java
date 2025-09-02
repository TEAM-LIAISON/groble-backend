package liaison.groble.persistence.content;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;

public interface JpaContentReviewRepository extends JpaRepository<ContentReview, Long> {

  boolean existsByUserIdAndContentIdAndReviewStatus(
      Long userId, Long contentId, ReviewStatus reviewStatus);

  boolean existsByGuestUserIdAndContentIdAndReviewStatus(
      Long guestUserId, Long contentId, ReviewStatus reviewStatus);
}
