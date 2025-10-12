package liaison.groble.persistence.notification.scheduled;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationSegment;

public interface JpaScheduledNotificationSegmentRepository
    extends JpaRepository<ScheduledNotificationSegment, Long> {

  List<ScheduledNotificationSegment> findByActiveTrueOrderByNameAsc();
}
