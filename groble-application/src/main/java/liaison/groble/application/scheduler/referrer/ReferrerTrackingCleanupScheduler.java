package liaison.groble.application.scheduler.referrer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import liaison.groble.application.dashboard.service.ReferrerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReferrerTrackingCleanupScheduler {

  private final ReferrerService referrerService;

  @Scheduled(cron = "0 10 4 * * ?", zone = "Asia/Seoul")
  public void purgeExpiredReferrerTracking() {
    long removed = referrerService.purgeTrackingOlderThanOneYear();
    if (removed > 0) {
      log.info("Referrer tracking cleanup removed {} records.", removed);
    } else {
      log.debug("Referrer tracking cleanup executed without deletions.");
    }
  }
}
