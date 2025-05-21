package liaison.groble.application.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.notification.dto.NotificationDetailsDto;
import liaison.groble.application.notification.dto.NotificationItemDto;
import liaison.groble.application.notification.dto.NotificationItemsDto;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.entity.SystemDetails;
import liaison.groble.domain.notification.enums.NotificationType;
import liaison.groble.domain.notification.enums.SubNotificationType;
import liaison.groble.domain.notification.repository.NotificationCustomRepository;
import liaison.groble.domain.notification.repository.NotificationRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationCustomRepository notificationCustomRepository;
  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

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
      case CERTIFY -> createCertifyDetails(notification, subType);
      case REVIEW -> createReviewDetails(notification, subType);
      case SYSTEM -> createSystemDetails(notification, subType);
      default -> null;
    };
  }

  private NotificationDetailsDto createCertifyDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.MAKER_CERTIFIED) {
      return NotificationDetailsDto.makerCertified(notification.getCertifyDetails().getNickname());
    } else if (subNotificationType == SubNotificationType.MAKER_CERTIFY_REJECTED) {
      return NotificationDetailsDto.makerCertifyRejected(
          notification.getCertifyDetails().getNickname());
    }
    return null;
  }

  private NotificationDetailsDto createReviewDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.CONTENT_REVIEW_APPROVED) {
      return NotificationDetailsDto.contentReviewApproved(
          notification.getReviewDetails().getContentId(),
          notification.getReviewDetails().getThumbnailUrl());
    } else if (subNotificationType == SubNotificationType.CONTENT_REVIEW_REJECTED) {
      return NotificationDetailsDto.contentReviewRejected(
          notification.getReviewDetails().getContentId(),
          notification.getReviewDetails().getThumbnailUrl());
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

  @Transactional
  public void sendWelcomeNotification(User user) {
    SystemDetails systemDetails =
        SystemDetails.welcomeGroble(user.getNickname(), "그로블에 오신 것을 환영합니다!");

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.SYSTEM,
            SubNotificationType.WELCOME_GROBLE,
            systemDetails);

    notificationRepository.save(notification);
    log.info("환영 알림 발송: userId={}", user.getId());
  }
}
