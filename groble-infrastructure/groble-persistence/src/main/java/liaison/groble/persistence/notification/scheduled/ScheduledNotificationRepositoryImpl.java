package liaison.groble.persistence.notification.scheduled;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.notification.scheduled.entity.ScheduledNotification;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;
import liaison.groble.domain.notification.scheduled.repository.ScheduledNotificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ScheduledNotificationRepositoryImpl implements ScheduledNotificationRepository {

  private final JpaScheduledNotificationRepository jpaScheduledNotificationRepository;

  @Override
  public ScheduledNotification save(ScheduledNotification scheduledNotification) {
    return jpaScheduledNotificationRepository.save(scheduledNotification);
  }

  @Override
  public Optional<ScheduledNotification> findById(Long id) {
    return jpaScheduledNotificationRepository.findById(id);
  }

  @Override
  public Page<ScheduledNotification> findAll(Pageable pageable) {
    return jpaScheduledNotificationRepository.findAll(pageable);
  }

  @Override
  public long countByStatus(ScheduledNotificationStatus status) {
    return jpaScheduledNotificationRepository.countByStatus(status);
  }

  @Override
  public long countAll() {
    return jpaScheduledNotificationRepository.count();
  }
}
