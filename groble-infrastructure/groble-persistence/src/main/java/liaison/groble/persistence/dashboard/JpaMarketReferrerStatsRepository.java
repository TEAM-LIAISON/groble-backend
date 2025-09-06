package liaison.groble.persistence.dashboard;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.MarketReferrerStats;

public interface JpaMarketReferrerStatsRepository extends JpaRepository<MarketReferrerStats, Long> {
  Optional<MarketReferrerStats> findByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long marketId, String referrerDomain, String source, String medium, String campaign);
}
