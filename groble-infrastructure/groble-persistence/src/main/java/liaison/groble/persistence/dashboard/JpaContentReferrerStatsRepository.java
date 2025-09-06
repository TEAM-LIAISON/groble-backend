package liaison.groble.persistence.dashboard;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;

public interface JpaContentReferrerStatsRepository
    extends JpaRepository<ContentReferrerStats, Long> {
  Optional<ContentReferrerStats> findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long contentId, String referrerDomain, String source, String medium, String campaign);
}
