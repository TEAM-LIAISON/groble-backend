package liaison.groble.domain.notification.scheduled.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationRun;

public interface ScheduledNotificationRunRepository {

  ScheduledNotificationRun save(ScheduledNotificationRun run);

  Page<ScheduledNotificationRun> findByScheduledNotificationId(
      Long scheduledNotificationId, Pageable pageable);

  List<ScheduledNotificationRun> findPendingRuns(LocalDateTime referenceTime);
}
