package liaison.groble.application.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.notification.dto.NotificationDetailsDto;
import liaison.groble.application.notification.dto.NotificationItemDto;
import liaison.groble.application.notification.dto.NotificationItemsDto;
import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.enums.NotificationType;
import liaison.groble.domain.notification.enums.SubNotificationType;
import liaison.groble.domain.notification.repository.NotificationCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationCustomRepository notificationCustomRepository;

  public NotificationItemsDto getNotificationItems(final Long userId) {
    List<Notification> notifications =
        notificationCustomRepository.getNotificationsByReceiverUser(userId);

    // Convert notifications to NotificationItemDto list
    List<NotificationItemDto> notificationItemDtos =
        notifications.stream().map(this::toNotificationItemDto).toList();

    // Build and return NotificationItemsDto
    return NotificationItemsDto.builder().notificationItems(notificationItemDtos).build();
  }

  /** 사용자의 모든 알림을 삭제합니다. */
  @Transactional
  public void deleteAllNotifications(final Long userId) {
    notificationCustomRepository.deleteAllNotificationsByReceiverUser(userId);
    log.info("모든 알림이 삭제되었습니다. userId: {}", userId);
  }

  /** 특정 알림을 삭제합니다. */
  @Transactional
  public void deleteNotification(final Long userId, final Long notificationId) {
    notificationCustomRepository.deleteNotificationByReceiverUser(userId, notificationId);
    log.info("알림이 삭제되었습니다. userId: {}, notificationId: {}", userId, notificationId);
  }

  /** Converts a Notification entity to a NotificationItemDto */
  private NotificationItemDto toNotificationItemDto(final Notification notification) {
    // 도메인 enum을 String으로 변환
    return NotificationItemDto.builder()
        .notificationId(notification.getId())
        .notificationType(notification.getNotificationType().name()) // enum을 String으로 변환
        .subNotificationType(notification.getSubNotificationType().name())
        .notificationReadStatus(notification.getNotificationReadStatus().name())
        .notificationOccurTime(notification.getCreatedAt())
        .notificationDetails(
            createNotificationDetails(
                notification,
                notification.getNotificationType(),
                notification.getSubNotificationType()))
        .build();
  }

  /** Creates appropriate NotificationDetailsDto based on notification type and subtype */
  private NotificationDetailsDto createNotificationDetails(
      final Notification notification,
      final NotificationType type,
      final SubNotificationType subType) {

    // Switch based on notification type and subtype to create appropriate details
    return switch (type) {
      case SELLER -> createSellerDetails(notification, subType);
      case CONTENT -> createContentDetails(notification, subType);
      case SYSTEM -> createSystemDetails(notification, subType);
      default -> null;
    };
  }

  private NotificationDetailsDto createSellerDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.SELLER_VERIFIED) {
      return NotificationDetailsDto.sellerVerified(
          notification.getSellerDetails().getNickname(),
          notification.getSellerDetails().getIsVerified());
    } else if (subNotificationType == SubNotificationType.SELLER_REJECTED) {
      return NotificationDetailsDto.sellerRejected(
          notification.getSellerDetails().getNickname(),
          notification.getSellerDetails().getIsVerified());
    }
    return null;
  }

  private NotificationDetailsDto createContentDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.CONTENT_APPROVED) {
      return NotificationDetailsDto.contentApproved(
          notification.getContentDetails().getContentId(),
          notification.getContentDetails().getThumbnailUrl(),
          notification.getContentDetails().getIsContentApproved());
    } else if (subNotificationType == SubNotificationType.CONTENT_REJECTED) {
      return NotificationDetailsDto.contentRejected(
          notification.getContentDetails().getContentId(),
          notification.getContentDetails().getThumbnailUrl(),
          notification.getContentDetails().getIsContentApproved());
    }
    return null;
  }

  private NotificationDetailsDto createSystemDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.WELCOME_GROBLE) {
      return NotificationDetailsDto.welcomeGroble(
          notification.getSystemDetails().getNickname(),
          notification.getSystemDetails().getSystemTitle());
    }
    return null;
  }
}
