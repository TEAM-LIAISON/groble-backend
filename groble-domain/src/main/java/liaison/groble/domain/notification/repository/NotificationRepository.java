package liaison.groble.domain.notification.repository;

import java.util.Optional;

import liaison.groble.domain.notification.entity.Notification;

public interface NotificationRepository {
  Notification save(Notification notification);

  Optional<Notification> findByIdAndUserId(Long notificationId, Long userId);

  long countUnreadByUserId(Long userId);
}
