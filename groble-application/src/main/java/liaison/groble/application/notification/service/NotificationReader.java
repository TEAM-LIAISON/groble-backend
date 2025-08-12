package liaison.groble.application.notification.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReader {
  private final NotificationRepository notificationRepository;

  public Notification getNotificationByIdAndUserId(Long notificationId, Long userId) {
    return notificationRepository
        .findByIdAndUserId(notificationId, userId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "알림을 찾을 수 없습니다. notificationId: " + notificationId + ", userId: " + userId));
  }

  // 읽지 않은 알림 개수 조회
  public long countUnreadNotificationsByUserId(Long userId) {
    return notificationRepository.countUnreadByUserId(userId);
  }
}
