package liaison.groble.api.server.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.notification.response.NotificationItems;
import liaison.groble.api.server.notification.mapper.NotificationDtoMapper;
import liaison.groble.application.notification.dto.NotificationItemsDto;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import lombok.RequiredArgsConstructor;

/** NotificationApi의 구현체 OpenAPI Generator를 활성화하면 NotificationApiDelegate를 구현하게 변경 */
@RestController
@RequiredArgsConstructor
public class NotificationApiController implements NotificationApi {

  private final NotificationService notificationService;
  private final NotificationDtoMapper notificationDtoMapper;

  @Override
  public ResponseEntity<GrobleResponse<Void>> deleteAllNotifications(Accessor accessor) {
    notificationService.deleteAllNotifications(accessor.getUserId());
    return ResponseEntity.ok(GrobleResponse.success(null, "모든 알림이 삭제되었습니다."));
  }

  @Override
  public ResponseEntity<GrobleResponse<Void>> deleteNotification(
      Accessor accessor, Long notificationId) {
    notificationService.deleteNotification(accessor.getUserId(), notificationId);
    return ResponseEntity.ok(GrobleResponse.success(null, "알림이 삭제되었습니다."));
  }

  @Override
  public ResponseEntity<GrobleResponse<NotificationItems>> getNotifications(Accessor accessor) {
    NotificationItemsDto notificationItemsDto =
        notificationService.getNotificationItems(accessor.getUserId());
    NotificationItems notificationItems =
        notificationDtoMapper.toNotificationItems(notificationItemsDto);
    return ResponseEntity.ok(GrobleResponse.success(notificationItems, "알림 조회 성공"));
  }
}
