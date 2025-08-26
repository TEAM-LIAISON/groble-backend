package liaison.groble.persistence.dashboard;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContentReferrerStatsRepositoryImpl implements ContentReferrerStatsRepository {
  private final JpaContentReferrerStatsRepository jpaContentReferrerStatsRepository;

  @Override
  public ContentReferrerStats save(ContentReferrerStats contentReferrerStats) {
    return jpaContentReferrerStatsRepository.save(contentReferrerStats);
  }
}
