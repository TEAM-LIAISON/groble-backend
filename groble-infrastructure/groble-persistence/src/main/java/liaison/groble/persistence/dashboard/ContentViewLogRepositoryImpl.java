package liaison.groble.persistence.dashboard;

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

  @Override
  public List<ContentViewLog> findByContentIdsAndViewedAtBetween(
      List<Long> contentIds, LocalDateTime start, LocalDateTime end) {
    if (contentIds == null || contentIds.isEmpty()) {
      return List.of();
    }
    return jpaContentViewLogRepository.findByContentIdInAndViewedAtBetween(contentIds, start, end);
  }

  @Override
  public Long countViews(List<Long> contentIds, LocalDateTime start, LocalDateTime end) {
    if (contentIds == null || contentIds.isEmpty()) {
      return 0L;
    }
    return jpaContentViewLogRepository.countViews(contentIds, start, end);
  }

  @Override
  public Long countDistinctViewers(List<Long> contentIds, LocalDateTime start, LocalDateTime end) {
    if (contentIds == null || contentIds.isEmpty()) {
      return 0L;
    }
    return jpaContentViewLogRepository.countDistinctViewers(contentIds, start, end);
  }
}
