package liaison.groble.persistence.dashboard;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.MarketReferrerStats;
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class MarketReferrerStatsRepositoryImpl implements MarketReferrerStatsRepository {
  private final JpaMarketReferrerStatsRepository jpaMarketReferrerStatsRepository;

  @Override
  public MarketReferrerStats save(MarketReferrerStats marketReferrerStats) {
    return jpaMarketReferrerStatsRepository.save(marketReferrerStats);
  }

  @Override
  public Optional<MarketReferrerStats> findByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long marketId, String referrerDomain, String source, String medium, String campaign) {
    return jpaMarketReferrerStatsRepository
        .findByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
            marketId, referrerDomain, source, medium, campaign);
  }
}
