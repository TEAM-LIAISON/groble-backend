package liaison.groble.domain.content.repository;

import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.domain.content.entity.ContentViewLog;

public interface ContentViewLogRepository {
  ContentViewLog save(ContentViewLog contentViewLog);

  List<ContentViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
