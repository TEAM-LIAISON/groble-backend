package liaison.groble.api.server.notification.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.notification.response.NotificationDetails;
import liaison.groble.api.model.notification.response.NotificationItem;
import liaison.groble.api.model.notification.response.NotificationItems;
import liaison.groble.application.notification.dto.NotificationDetailsDto;
import liaison.groble.application.notification.dto.NotificationItemDto;
import liaison.groble.application.notification.dto.NotificationItemsDto;

@Component
public class NotificationDtoMapper {

  /** NotificationItemsDto를 API 응답 모델인 NotificationItems로 변환 */
  public NotificationItems toNotificationItems(final NotificationItemsDto notificationItemsDto) {
    if (notificationItemsDto == null) {
      return null;
    }

    List<NotificationItem> notificationItems =
        notificationItemsDto.getNotificationItems().stream()
            .map(this::toNotificationItem)
            .collect(Collectors.toList());

    return NotificationItems.builder().notificationItems(notificationItems).build();
  }

  /** 개별 NotificationItemDto를 NotificationItem으로 변환 - 이제 enum 변환이 필요 없음 (이미 String 타입) */
  private NotificationItem toNotificationItem(NotificationItemDto dto) {
    if (dto == null) {
      return null;
    }

    return NotificationItem.builder()
        .notificationId(dto.getNotificationId())
        .notificationType(dto.getNotificationType()) // 이미 String이므로 변환 불필요
        .subNotificationType(dto.getSubNotificationType())
        .notificationReadStatus(dto.getNotificationReadStatus())
        .notificationOccurTime(dto.getNotificationOccurTime())
        .notificationDetails(toNotificationDetails(dto.getNotificationDetails()))
        .build();
  }

  /** NotificationDetailsDto를 NotificationDetails로 변환 */
  private NotificationDetails toNotificationDetails(NotificationDetailsDto dto) {
    if (dto == null) {
      return null;
    }

    return NotificationDetails.builder()
        .nickname(dto.getNickname())
        .isVerified(dto.getIsVerified())
        .contentId(dto.getContentId())
        .thumbnailUrl(dto.getThumbnailUrl())
        .isContentApproved(dto.getIsContentApproved())
        .systemTitle(dto.getSystemTitle())
        .build();
  }
}
