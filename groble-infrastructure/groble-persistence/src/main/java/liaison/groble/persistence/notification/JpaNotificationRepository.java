package liaison.groble.persistence.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.notification.entity.Notification;

public interface JpaNotificationRepository extends JpaRepository<Notification, Long> {
  @Query("SELECT n FROM Notification n WHERE n.id = :notificationId AND n.user.id = :userId")
  Optional<Notification> findByIdAndUserId(
      @Param("notificationId") Long notificationId, @Param("userId") Long userId);

  @Query(
      value =
          """
  SELECT COUNT(*)
  FROM notifications
  WHERE receiver_user_id = :userId
    AND notification_read_status = 'UNREAD'
""",
      nativeQuery = true)
  long countUnreadByUserId(@Param("userId") Long userId);
}
