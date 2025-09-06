package liaison.groble.domain.dashboard.repository;

import java.util.Optional;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;

public interface ContentReferrerStatsRepository {
  ContentReferrerStats save(ContentReferrerStats contentReferrerStats);

  Optional<ContentReferrerStats> findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long contentId, String referrerDomain, String source, String medium, String campaign);
}
