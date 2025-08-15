package liaison.groble.persistence.content;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.ContentViewLog;

public interface JpaContentViewLogRepository extends JpaRepository<ContentViewLog, Long> {
  List<ContentViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
