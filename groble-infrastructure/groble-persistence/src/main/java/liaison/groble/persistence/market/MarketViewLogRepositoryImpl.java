package liaison.groble.persistence.market;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.market.entity.MarketViewLog;
import liaison.groble.domain.market.repository.MarketViewLogRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class MarketViewLogRepositoryImpl implements MarketViewLogRepository {
  private final JpaMarketViewLogRepository jpaMarketViewLogRepository;

  @Override
  public MarketViewLog save(MarketViewLog marketViewLog) {
    return jpaMarketViewLogRepository.save(marketViewLog);
  }

  @Override
  public List<MarketViewLog> findByViewedAtBetween(LocalDateTime start, LocalDateTime end) {
    return jpaMarketViewLogRepository.findByViewedAtBetween(start, end);
  }
}
