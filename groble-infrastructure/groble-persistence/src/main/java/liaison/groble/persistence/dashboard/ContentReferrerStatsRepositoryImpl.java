package liaison.groble.persistence.dashboard;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContentReferrerStatsRepositoryImpl implements ContentReferrerStatsRepository {
  private final JpaContentReferrerStatsRepository jpaContentReferrerStatsRepository;

  @Override
  public ContentReferrerStats save(ContentReferrerStats contentReferrerStats) {
    return jpaContentReferrerStatsRepository.save(contentReferrerStats);
  }

  @Override
  public void delete(ContentReferrerStats contentReferrerStats) {
    jpaContentReferrerStatsRepository.delete(contentReferrerStats);
  }

  @Override
  public Optional<ContentReferrerStats>
      findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
          Long contentId, String referrerDomain, String source, String medium, String campaign) {
    return jpaContentReferrerStatsRepository
        .findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
            contentId, referrerDomain, source, medium, campaign);
  }

  @Override
  public List<ContentReferrerStats>
      findAllByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
          Long contentId, String referrerDomain, String source, String medium, String campaign) {
    return jpaContentReferrerStatsRepository
        .findAllByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
            contentId, referrerDomain, source, medium, campaign);
  }
}
