package liaison.groble.persistence.dashboard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.dashboard.entity.MarketReferrerStats;

public interface JpaMarketReferrerStatsRepository extends JpaRepository<MarketReferrerStats, Long> {
  Optional<MarketReferrerStats> findByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long marketId, String referrerDomain, String source, String medium, String campaign);

  @Query(
      "SELECT m FROM MarketReferrerStats m WHERE m.marketId = :marketId "
          + "AND m.referrerDomain = :referrerDomain AND m.source = :source AND m.medium = :medium "
          + "AND (:campaign IS NULL AND m.campaign IS NULL OR m.campaign = :campaign)")
  List<MarketReferrerStats> findAllByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      @Param("marketId") Long marketId,
      @Param("referrerDomain") String referrerDomain,
      @Param("source") String source,
      @Param("medium") String medium,
      @Param("campaign") String campaign);
}
