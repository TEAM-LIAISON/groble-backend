package liaison.groble.persistence.notification;

import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.repository.NotificationCustomRepository;
import liaison.groble.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {
  private final JpaNotificationRepository jpaNotificationRepository;
  private final NotificationCustomRepository notificationCustomRepository;

  public List<Notification> getNotificationsByReceiverUser(final Long userId) {
    return notificationCustomRepository.getNotificationsByReceiverUser(userId);
  }
}
