package liaison.groble.persistence.content;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.repository.ContentViewStatsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContentViewStatsRepositoryImpl implements ContentViewStatsRepository {
  private final JpaContentViewStatsRepository jpaContentViewStatsRepository;
}
