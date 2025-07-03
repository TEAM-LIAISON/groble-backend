package liaison.groble.persistence.content;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.repository.ContentReviewRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReviewRepositoryImpl implements ContentReviewRepository {
  private final JpaContentReviewRepository jpaContentReviewRepository;

  public ContentReview save(ContentReview contentReview) {
    return jpaContentReviewRepository.save(contentReview);
  }
}
