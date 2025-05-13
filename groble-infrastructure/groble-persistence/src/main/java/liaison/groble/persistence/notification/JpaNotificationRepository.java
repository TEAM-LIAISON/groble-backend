package liaison.groble.persistence.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.notification.entity.Notification;

public interface JpaNotificationRepository extends JpaRepository<Notification, Long> {}
