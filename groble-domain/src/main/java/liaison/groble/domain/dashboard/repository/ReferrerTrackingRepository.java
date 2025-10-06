package liaison.groble.domain.dashboard.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import liaison.groble.domain.dashboard.entity.ReferrerTracking;

public interface ReferrerTrackingRepository {

  ReferrerTracking save(ReferrerTracking referrerTracking);

  Optional<ReferrerTracking> findRecentContentTracking(String sessionId, String contentId);

  Optional<ReferrerTracking> findRecentMarketTracking(String sessionId, String marketLinkUrl);

  long deleteAllByCreatedAtBefore(LocalDateTime threshold);
}
