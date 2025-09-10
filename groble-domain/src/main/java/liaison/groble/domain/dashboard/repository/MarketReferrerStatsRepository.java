package liaison.groble.domain.dashboard.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.dashboard.entity.MarketReferrerStats;

public interface MarketReferrerStatsRepository {
  MarketReferrerStats save(MarketReferrerStats marketReferrerStats);

  void delete(MarketReferrerStats marketReferrerStats);

  Optional<MarketReferrerStats> findByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long marketId, String referrerDomain, String source, String medium, String campaign);

  List<MarketReferrerStats> findAllByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long marketId, String referrerDomain, String source, String medium, String campaign);
}
