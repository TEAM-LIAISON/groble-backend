package liaison.groble.domain.notification.repository;

import java.util.List;

import liaison.groble.domain.notification.entity.Notification;

public interface NotificationCustomRepository {
  List<Notification> getNotificationsByReceiverUser(final Long userId);
}
