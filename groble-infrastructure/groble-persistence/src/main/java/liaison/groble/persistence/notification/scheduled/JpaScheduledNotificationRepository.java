package liaison.groble.persistence.notification.scheduled;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotification;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;

public interface JpaScheduledNotificationRepository
    extends JpaRepository<ScheduledNotification, Long> {

  long countByStatus(ScheduledNotificationStatus status);
}
