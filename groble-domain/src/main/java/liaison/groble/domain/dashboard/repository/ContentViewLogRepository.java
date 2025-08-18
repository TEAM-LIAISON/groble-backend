package liaison.groble.domain.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.domain.dashboard.entity.ContentViewLog;

public interface ContentViewLogRepository {
  ContentViewLog save(ContentViewLog contentViewLog);

  List<ContentViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
