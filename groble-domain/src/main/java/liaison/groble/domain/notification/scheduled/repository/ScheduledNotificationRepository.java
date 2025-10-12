package liaison.groble.domain.notification.scheduled.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotification;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;

public interface ScheduledNotificationRepository {

  ScheduledNotification save(ScheduledNotification scheduledNotification);

  Optional<ScheduledNotification> findById(Long id);

  Page<ScheduledNotification> findAll(Pageable pageable);

  long countByStatus(ScheduledNotificationStatus status);

  long countAll();
}
