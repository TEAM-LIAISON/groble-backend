package liaison.groble.persistence.dashboard;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.ReferrerTracking;
import liaison.groble.domain.dashboard.repository.ReferrerTrackingRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReferrerTrackingRepositoryImpl implements ReferrerTrackingRepository {

  private final JpaReferrerTrackingRepository jpaReferrerTrackingRepository;

  @Override
  public ReferrerTracking save(ReferrerTracking referrerTracking) {
    return jpaReferrerTrackingRepository.save(referrerTracking);
  }

  @Override
  public Optional<ReferrerTracking> findRecentContentTracking(String sessionId, String contentId) {
    return jpaReferrerTrackingRepository.findFirstBySessionIdAndContentIdOrderByCreatedAtDesc(
        sessionId, contentId);
  }

  @Override
  public Optional<ReferrerTracking> findRecentMarketTracking(
      String sessionId, String marketLinkUrl) {
    return jpaReferrerTrackingRepository.findFirstBySessionIdAndMarketLinkUrlOrderByCreatedAtDesc(
        sessionId, marketLinkUrl);
  }

  @Override
  public Optional<ReferrerTracking> findLatestMarketNavigation(String sessionId) {
    return jpaReferrerTrackingRepository
        .findFirstBySessionIdAndMarketLinkUrlIsNotNullOrderByCreatedAtDesc(sessionId);
  }

  @Override
  public long deleteAllByCreatedAtBefore(LocalDateTime threshold) {
    return jpaReferrerTrackingRepository.deleteByCreatedAtBefore(threshold);
  }
}
