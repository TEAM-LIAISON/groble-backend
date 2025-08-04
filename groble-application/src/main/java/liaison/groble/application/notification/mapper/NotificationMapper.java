package liaison.groble.application.notification.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.entity.detail.CertifyDetails;
import liaison.groble.domain.notification.entity.detail.PurchaseDetails;
import liaison.groble.domain.notification.entity.detail.ReviewDetails;
import liaison.groble.domain.notification.entity.detail.SellDetails;
import liaison.groble.domain.notification.entity.detail.SystemDetails;
import liaison.groble.domain.notification.enums.NotificationReadStatus;
import liaison.groble.domain.notification.enums.NotificationType;
import liaison.groble.domain.notification.enums.SubNotificationType;
import liaison.groble.domain.user.entity.User;

@Component
public class NotificationMapper {

  private final UserReader userReader;

  public NotificationMapper(UserReader userReader) {
    this.userReader = userReader;
  }

  public Notification toNotification(
      Long notificationReceiverMemberId,
      NotificationType notificationType,
      SubNotificationType subNotificationType,
      Object detailObject) {
    User receiver = userReader.getUserById(notificationReceiverMemberId);

    Notification.NotificationBuilder builder =
        Notification.builder()
            .user(receiver)
            .notificationType(notificationType)
            .subNotificationType(subNotificationType)
            .notificationReadStatus(NotificationReadStatus.UNREAD)
            .createdAt(LocalDateTime.now());

    // 알림 타입별로 세부 정보 설정
    switch (notificationType) {
      case SYSTEM:
        switch (subNotificationType) {
          case WELCOME_GROBLE -> builder.systemDetails(
              SystemDetails.builder()
                  .nickname(((SystemDetails) detailObject).getNickname())
                  .systemTitle(((SystemDetails) detailObject).getSystemTitle())
                  .build());
        }
        break;
      case REVIEW:
        switch (subNotificationType) {
          case CONTENT_REVIEWED -> builder.reviewDetails(
              ReviewDetails.builder()
                  .contentId(((ReviewDetails) detailObject).getContentId())
                  .thumbnailUrl(((ReviewDetails) detailObject).getThumbnailUrl())
                  .build());
        }
        break;
      case CERTIFY:
        switch (subNotificationType) {
          case MAKER_CERTIFIED, MAKER_CERTIFY_REJECTED -> builder.certifyDetails(
              CertifyDetails.builder()
                  .nickname(((CertifyDetails) detailObject).getNickname())
                  .build());
        }
        break;
      case PURCHASE:
        switch (subNotificationType) {
          case CONTENT_PURCHASED -> builder.purchaseDetails(
              PurchaseDetails.builder()
                  .contentId(((PurchaseDetails) detailObject).getContentId())
                  .merchantUid(((PurchaseDetails) detailObject).getMerchantUid())
                  .build());
          case CONTENT_REVIEW_REPLY -> builder.purchaseDetails(
              PurchaseDetails.builder()
                  .contentId(((PurchaseDetails) detailObject).getContentId())
                  .reviewId(((PurchaseDetails) detailObject).getReviewId())
                  .build());
        }
        break;

      case SELL:
        switch (subNotificationType) {
          case CONTENT_SOLD, CONTENT_SOLD_STOPPED -> builder.sellDetails(
              SellDetails.builder().contentId(((SellDetails) detailObject).getContentId()).build());
        }
        break;
      default:
        throw new IllegalArgumentException("지원하지 않는 알림 타입입니다: " + notificationType);
    }

    return builder.build();
  }
}
