package liaison.groble.persistence.notification.scheduled;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationRun;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationRunStatus;

public interface JpaScheduledNotificationRunRepository
    extends JpaRepository<ScheduledNotificationRun, Long> {

  Page<ScheduledNotificationRun> findByScheduledNotificationIdOrderByExecutionTimeDesc(
      Long scheduledNotificationId, Pageable pageable);

  List<ScheduledNotificationRun> findByStatusAndExecutionTimeLessThanEqual(
      ScheduledNotificationRunStatus status, LocalDateTime time);
}
