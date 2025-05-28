package liaison.groble.domain.notification.repository;

import liaison.groble.domain.notification.entity.Notification;

public interface NotificationRepository {
  Notification save(Notification notification);
}
