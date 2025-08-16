package liaison.groble.domain.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.domain.dashboard.entity.MarketViewLog;

public interface MarketViewLogRepository {
  MarketViewLog save(MarketViewLog marketViewLog);

  List<MarketViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
