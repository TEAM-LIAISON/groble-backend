package liaison.groble.persistence.market;

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
}
