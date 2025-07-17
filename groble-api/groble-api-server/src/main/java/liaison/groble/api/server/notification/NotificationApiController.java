package liaison.groble.api.server.notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.notification.response.NotificationItemsResponse;
import liaison.groble.application.notification.dto.NotificationItemsDTO;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.notification.NotificationMapper;

import lombok.RequiredArgsConstructor;

/** NotificationApi의 구현체 OpenAPI Generator를 활성화하면 NotificationApiDelegate를 구현하게 변경 */
@RestController
@RequiredArgsConstructor
public class NotificationApiController implements NotificationApi {

  // 응답 메시지 상수화
  private static final String DELETE_ALL_SUCCESS_MESSAGE = "모든 알림이 삭제되었습니다.";
  private static final String DELETE_SUCCESS_MESSAGE = "알림이 삭제되었습니다.";
  private static final String GET_SUCCESS_MESSAGE = "알림 조회 성공";

  // Service
  private final NotificationService notificationService;

  // Mapper
  private final NotificationMapper notificationMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @Override
  public ResponseEntity<GrobleResponse<Void>> deleteAllNotifications(Accessor accessor) {
    notificationService.deleteAllNotifications(accessor.getUserId());
    return responseHelper.success(null, DELETE_ALL_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<GrobleResponse<Void>> deleteNotification(
      Accessor accessor, Long notificationId) {
    notificationService.deleteNotification(accessor.getUserId(), notificationId);
    return responseHelper.success(null, DELETE_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<GrobleResponse<NotificationItemsResponse>> getNotifications(
      Accessor accessor) {
    NotificationItemsDTO notificationItemsDTO =
        notificationService.getNotificationItems(accessor.getUserId());
    NotificationItemsResponse notificationItemsResponse =
        notificationMapper.toNotificationItemsResponse(notificationItemsDTO);
    return responseHelper.success(notificationItemsResponse, GET_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
