package liaison.groble.api.server.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.notification.response.NotificationItems;
import liaison.groble.api.server.notification.mapper.NotificationDtoMapper;
import liaison.groble.application.notification.dto.NotificationItemsDto;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림 관련 API", description = "알림 삭제, 알림 조회 API")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationDtoMapper notificationDtoMapper;

  public NotificationController(
      NotificationService notificationService, NotificationDtoMapper notificationDtoMapper) {
    this.notificationService = notificationService;
    this.notificationDtoMapper = notificationDtoMapper;
  }

  // 알림 전체 삭제

  // 알림 단일 삭제

  // 알림 전체 조회
  public ResponseEntity<GrobleResponse<NotificationItems>> getNotificationItems(
      @Auth final Accessor accessor) {
    NotificationItemsDto notificationItemsDto =
        notificationService.getNotificationItems(accessor.getUserId());
    NotificationItems notificationItems =
        notificationDtoMapper.toNotificationItems(notificationItemsDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "알림 목록 전체 조회 성공"));
  }
}
