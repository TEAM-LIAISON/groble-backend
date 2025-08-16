package liaison.groble.persistence.market;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.MarketViewLog;

public interface JpaMarketViewLogRepository extends JpaRepository<MarketViewLog, Long> {
  List<MarketViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
