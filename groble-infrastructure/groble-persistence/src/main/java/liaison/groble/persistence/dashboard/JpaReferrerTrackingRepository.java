package liaison.groble.persistence.dashboard;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.ReferrerTracking;

public interface JpaReferrerTrackingRepository extends JpaRepository<ReferrerTracking, Long> {

  Optional<ReferrerTracking> findFirstBySessionIdAndContentIdOrderByCreatedAtDesc(
      String sessionId, String contentId);

  Optional<ReferrerTracking> findFirstBySessionIdAndMarketLinkUrlOrderByCreatedAtDesc(
      String sessionId, String marketLinkUrl);

  Optional<ReferrerTracking> findFirstBySessionIdAndMarketLinkUrlIsNotNullOrderByCreatedAtDesc(
      String sessionId);

  long deleteByCreatedAtBefore(LocalDateTime threshold);
}
