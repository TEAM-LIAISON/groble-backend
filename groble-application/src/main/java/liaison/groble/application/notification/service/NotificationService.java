package liaison.groble.application.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.notification.dto.NotificationDetailsDTO;
import liaison.groble.application.notification.dto.NotificationItemDTO;
import liaison.groble.application.notification.dto.NotificationItemsDTO;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.entity.detail.CertifyDetails;
import liaison.groble.domain.notification.entity.detail.PurchaseDetails;
import liaison.groble.domain.notification.entity.detail.ReviewDetails;
import liaison.groble.domain.notification.entity.detail.SellDetails;
import liaison.groble.domain.notification.entity.detail.SystemDetails;
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
  private final NotificationReader notificationReader;
  private final NotificationMapper notificationMapper;

  public NotificationItemsDTO getNotificationItems(final Long userId) {
    List<Notification> notifications =
        notificationCustomRepository.getNotificationsByReceiverUser(userId);

    // Convert notifications to NotificationItemDTO list
    List<NotificationItemDTO> notificationItemDTOS =
        notifications.stream().map(this::toNotificationItemDTO).toList();

    // Build and return NotificationItemsDTO
    return NotificationItemsDTO.builder().notificationItems(notificationItemDTOS).build();
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

  /** Converts a Notification entity to a NotificationItemDTO */
  private NotificationItemDTO toNotificationItemDTO(final Notification notification) {
    // 도메인 enum을 String으로 변환
    return NotificationItemDTO.builder()
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

  /** Creates appropriate NotificationDetailsDTO based on notification type and subtype */
  private NotificationDetailsDTO createNotificationDetails(
      final Notification notification,
      final NotificationType type,
      final SubNotificationType subType) {

    // Switch based on notification type and subtype to create appropriate details
    return switch (type) {
      case CERTIFY -> createCertifyDetails(notification, subType);
      case REVIEW -> createReviewDetails(notification, subType);
      case SYSTEM -> createSystemDetails(notification, subType);
      case PURCHASE -> createPurchaseDetails(notification, subType);
      case SELL -> createSellDetails(notification, subType);
      default -> null;
    };
  }

  private NotificationDetailsDTO createCertifyDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.MAKER_CERTIFIED) {
      return NotificationDetailsDTO.makerCertified(notification.getCertifyDetails().getNickname());
    } else if (subNotificationType == SubNotificationType.MAKER_CERTIFY_REJECTED) {
      return NotificationDetailsDTO.makerCertifyRejected(
          notification.getCertifyDetails().getNickname());
    }
    return null;
  }

  private NotificationDetailsDTO createReviewDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.CONTENT_REVIEWED) {
      return NotificationDetailsDTO.contentReviewed(
          notification.getReviewDetails().getContentId(),
          notification.getReviewDetails().getReviewId(),
          notification.getReviewDetails().getThumbnailUrl());
    }
    return null;
  }

  private NotificationDetailsDTO createSystemDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.WELCOME_GROBLE) {
      return NotificationDetailsDTO.welcomeGroble(
          notification.getSystemDetails().getNickname(),
          notification.getSystemDetails().getSystemTitle());
    }
    return null;
  }

  private NotificationDetailsDTO createPurchaseDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.CONTENT_REVIEW_REPLY) {
      return NotificationDetailsDTO.contentReviewReplied(
          notification.getPurchaseDetails().getContentId(),
          notification.getPurchaseDetails().getReviewId());
    } else if (subNotificationType == SubNotificationType.CONTENT_PURCHASED) {
      return NotificationDetailsDTO.contentPurchased(
          notification.getPurchaseDetails().getContentId(),
          notification.getPurchaseDetails().getMerchantUid());
    }
    return null;
  }

  private NotificationDetailsDTO createSellDetails(
      Notification notification, SubNotificationType subNotificationType) {
    // 콘텐츠 판매 [✅ 상품이 판매됐어요]
    if (subNotificationType == SubNotificationType.CONTENT_SOLD) {
      return NotificationDetailsDTO.contentSold(
          notification.getSellDetails().getContentId(),
          notification.getSellDetails().getPurchaseId(),
          notification.getSellDetails().getThumbnailUrl());
    }
    // 콘텐츠 판매 중단 [✅ 상품 판매가 중단됐어요]
    else if (subNotificationType == SubNotificationType.CONTENT_SOLD_STOPPED) {
      return NotificationDetailsDTO.contentSoldStopped(
          notification.getSellDetails().getContentId(),
          notification.getSellDetails().getThumbnailUrl());
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
  }

  @Transactional
  public void sendMakerCertifiedVerificationNotification(User user) {
    CertifyDetails certifyDetails = CertifyDetails.builder().nickname(user.getNickname()).build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.CERTIFY,
            SubNotificationType.MAKER_CERTIFIED,
            certifyDetails);

    notificationRepository.save(notification);
    log.info("메이커 인증 승인 알림 발송: userId={}", user.getId());
  }

  @Transactional
  public void sendMakerRejectedVerificationNotificationAsync(Long userId, String nickname) {
    CertifyDetails certifyDetails = CertifyDetails.builder().nickname(nickname).build();

    Notification notification =
        notificationMapper.toNotification(
            userId,
            NotificationType.CERTIFY,
            SubNotificationType.MAKER_CERTIFY_REJECTED,
            certifyDetails);

    notificationRepository.save(notification);
  }

  @Transactional
  public void sendContentSoldNotification(
      User user, Long contentId, Long purchaseId, String thumbnailUrl) {
    SellDetails sellDetails =
        SellDetails.builder()
            .contentId(contentId)
            .purchaseId(purchaseId)
            .thumbnailUrl(thumbnailUrl)
            .build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(), NotificationType.SELL, SubNotificationType.CONTENT_SOLD, sellDetails);
    notificationRepository.save(notification);
  }

  @Transactional
  public void sendContentPurchasedNotification(
      User user, Long contentId, String merchantUid, String thumbnailUrl) {
    PurchaseDetails purchaseDetails =
        PurchaseDetails.builder()
            .contentId(contentId)
            .merchantUid(merchantUid)
            .thumbnailUrl(thumbnailUrl)
            .build();
    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.PURCHASE,
            SubNotificationType.CONTENT_PURCHASED,
            purchaseDetails);
    notificationRepository.save(notification);
  }

  @Transactional
  public void sendContentReviewReplyNotification(
      User user, Long contentId, Long reviewId, String thumbnailUrl) {
    PurchaseDetails purchaseDetails =
        PurchaseDetails.builder()
            .contentId(contentId)
            .reviewId(reviewId)
            .thumbnailUrl(thumbnailUrl)
            .build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.PURCHASE,
            SubNotificationType.CONTENT_REVIEW_REPLY,
            purchaseDetails);

    notificationRepository.save(notification);
    log.info("콘텐츠 리뷰 답글 알림 발송: userId={}, contentId={}", user.getId(), contentId);
  }

  @Transactional
  public void sendContentReviewNotification(
      User user, Long contentId, Long reviewId, String thumbnailUrl) {
    ReviewDetails reviewDetails =
        ReviewDetails.builder()
            .contentId(contentId)
            .reviewId(reviewId)
            .thumbnailUrl(thumbnailUrl)
            .build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.REVIEW,
            SubNotificationType.CONTENT_REVIEWED,
            reviewDetails);

    notificationRepository.save(notification);
    log.info("콘텐츠 리뷰 알림 발송: userId={}, contentId={}", user.getId(), contentId);
  }

  @Transactional
  public void readNotification(Long userId, Long notificationId) {
    Notification notification =
        notificationReader.getNotificationByIdAndUserId(userId, notificationId);
    notification.markAsRead();
  }
}
