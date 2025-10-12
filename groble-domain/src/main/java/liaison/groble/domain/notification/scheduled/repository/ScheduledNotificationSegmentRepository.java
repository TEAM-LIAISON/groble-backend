package liaison.groble.domain.notification.scheduled.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationSegment;

public interface ScheduledNotificationSegmentRepository {

  ScheduledNotificationSegment save(ScheduledNotificationSegment segment);

  List<ScheduledNotificationSegment> findActiveSegments();

  Optional<ScheduledNotificationSegment> findById(Long id);
}
