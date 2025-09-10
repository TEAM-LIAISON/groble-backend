package liaison.groble.persistence.dashboard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;

public interface JpaContentReferrerStatsRepository
    extends JpaRepository<ContentReferrerStats, Long> {
  Optional<ContentReferrerStats> findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long contentId, String referrerDomain, String source, String medium, String campaign);

  @Query(
      "SELECT c FROM ContentReferrerStats c WHERE c.contentId = :contentId "
          + "AND c.referrerDomain = :referrerDomain AND c.source = :source AND c.medium = :medium "
          + "AND (:campaign IS NULL AND c.campaign IS NULL OR c.campaign = :campaign)")
  List<ContentReferrerStats> findAllByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      @Param("contentId") Long contentId,
      @Param("referrerDomain") String referrerDomain,
      @Param("source") String source,
      @Param("medium") String medium,
      @Param("campaign") String campaign);
}
