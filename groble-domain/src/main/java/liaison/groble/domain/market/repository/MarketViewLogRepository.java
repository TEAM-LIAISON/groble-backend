package liaison.groble.domain.market.repository;

import java.time.LocalDateTime;
import java.util.List;

import liaison.groble.domain.market.entity.MarketViewLog;

public interface MarketViewLogRepository {
  MarketViewLog save(MarketViewLog marketViewLog);

  List<MarketViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
