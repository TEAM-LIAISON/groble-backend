package liaison.groble.persistence.content;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.ContentViewLog;
import liaison.groble.domain.dashboard.repository.ContentViewLogRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContentViewLogRepositoryImpl implements ContentViewLogRepository {

  private final JpaContentViewLogRepository jpaContentViewLogRepository;

  @Override
  public ContentViewLog save(ContentViewLog contentViewLog) {
    return jpaContentViewLogRepository.save(contentViewLog);
  }

  @Override
  public List<ContentViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end) {
    return jpaContentViewLogRepository.findByViewedAtBetween(start, end);
  }
}
