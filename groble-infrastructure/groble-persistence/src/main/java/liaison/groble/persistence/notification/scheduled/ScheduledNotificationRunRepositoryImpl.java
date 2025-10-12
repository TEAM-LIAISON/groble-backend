package liaison.groble.persistence.notification.scheduled;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationRun;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationRunStatus;
import liaison.groble.domain.notification.scheduled.repository.ScheduledNotificationRunRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ScheduledNotificationRunRepositoryImpl implements ScheduledNotificationRunRepository {

  private final JpaScheduledNotificationRunRepository jpaScheduledNotificationRunRepository;

  @Override
  public ScheduledNotificationRun save(ScheduledNotificationRun run) {
    return jpaScheduledNotificationRunRepository.save(run);
  }

  @Override
  public Page<ScheduledNotificationRun> findByScheduledNotificationId(
      Long scheduledNotificationId, Pageable pageable) {
    return jpaScheduledNotificationRunRepository
        .findByScheduledNotificationIdOrderByExecutionTimeDesc(scheduledNotificationId, pageable);
  }

  @Override
  public List<ScheduledNotificationRun> findPendingRuns(LocalDateTime referenceTime) {
    return jpaScheduledNotificationRunRepository.findByStatusAndExecutionTimeLessThanEqual(
        ScheduledNotificationRunStatus.PENDING, referenceTime);
  }
}
