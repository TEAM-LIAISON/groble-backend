package liaison.groble.persistence.dashboard;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.MarketReferrerEvent;
import liaison.groble.domain.dashboard.repository.MarketReferrerEventRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MarketReferrerEventRepositoryImpl implements MarketReferrerEventRepository {
  private final JpaMarketReferrerEventRepository jpaMarketReferrerEventRepository;

  @Override
  public MarketReferrerEvent save(MarketReferrerEvent marketReferrerEvent) {
    return jpaMarketReferrerEventRepository.save(marketReferrerEvent);
  }
}
