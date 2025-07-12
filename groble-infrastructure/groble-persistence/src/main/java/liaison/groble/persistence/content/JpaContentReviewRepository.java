package liaison.groble.persistence.content;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.ContentReview;

public interface JpaContentReviewRepository extends JpaRepository<ContentReview, Long> {

  boolean existsByUserIdAndContentId(Long userId, Long contentId);
}
