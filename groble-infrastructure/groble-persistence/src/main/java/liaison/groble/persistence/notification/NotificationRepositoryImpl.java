package liaison.groble.persistence.notification;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {
  private final JpaNotificationRepository jpaNotificationRepository;

  @Override
  public Notification save(final Notification notification) {
    return jpaNotificationRepository.save(notification);
  }

  @Override
  public Optional<Notification> findByIdAndUserId(Long notificationId, Long userId) {
    return jpaNotificationRepository.findByIdAndUserId(notificationId, userId);
  }

  @Override
  public long countUnreadByUserId(Long userId) {
    return jpaNotificationRepository.countUnreadByUserId(userId);
  }
}
