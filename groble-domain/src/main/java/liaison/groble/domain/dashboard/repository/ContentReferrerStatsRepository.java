package liaison.groble.domain.dashboard.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;

public interface ContentReferrerStatsRepository {
  ContentReferrerStats save(ContentReferrerStats contentReferrerStats);

  void delete(ContentReferrerStats contentReferrerStats);

  Optional<ContentReferrerStats> findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long contentId, String referrerDomain, String source, String medium, String campaign);

  List<ContentReferrerStats> findAllByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
      Long contentId, String referrerDomain, String source, String medium, String campaign);
}
