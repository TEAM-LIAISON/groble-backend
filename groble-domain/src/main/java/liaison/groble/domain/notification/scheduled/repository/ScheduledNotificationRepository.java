package liaison.groble.domain.notification.scheduled.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.notification.scheduled.dto.ScheduledNotificationChannelAggregate;
import liaison.groble.domain.notification.scheduled.dto.ScheduledNotificationStatisticsAggregate;
import liaison.groble.domain.notification.scheduled.entity.ScheduledNotification;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;

public interface ScheduledNotificationRepository {

  ScheduledNotification save(ScheduledNotification scheduledNotification);

  Optional<ScheduledNotification> findById(Long id);

  Page<ScheduledNotification> findAll(Pageable pageable);

  long countByStatus(ScheduledNotificationStatus status);

  long countAll();

  ScheduledNotificationStatisticsAggregate aggregateStatistics(
      LocalDateTime startDateTime, LocalDateTime endDateTime, ScheduledNotificationChannel channel);

  List<ScheduledNotificationChannelAggregate> aggregateStatisticsByChannel(
      LocalDateTime startDateTime, LocalDateTime endDateTime, ScheduledNotificationChannel channel);
}
