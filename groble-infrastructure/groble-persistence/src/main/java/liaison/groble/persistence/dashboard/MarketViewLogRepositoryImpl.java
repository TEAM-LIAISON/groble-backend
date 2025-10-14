package liaison.groble.persistence.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.MarketViewLog;
import liaison.groble.domain.dashboard.repository.MarketViewLogRepository;

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

  @Override
  public List<MarketViewLog> findByMarketIdAndViewedAtBetween(
      Long marketId, LocalDateTime start, LocalDateTime end) {
    if (marketId == null) {
      return List.of();
    }
    return jpaMarketViewLogRepository.findByMarketIdAndViewedAtBetween(marketId, start, end);
  }

  @Override
  public Long countViews(Long marketId, LocalDateTime start, LocalDateTime end) {
    if (marketId == null) {
      return 0L;
    }
    return jpaMarketViewLogRepository.countViews(marketId, start, end);
  }

  @Override
  public Long countDistinctViewers(Long marketId, LocalDateTime start, LocalDateTime end) {
    if (marketId == null) {
      return 0L;
    }
    return jpaMarketViewLogRepository.countDistinctViewers(marketId, start, end);
  }
}
