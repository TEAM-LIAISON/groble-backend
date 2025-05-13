package liaison.groble.api.server.notification;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.server.notification.mapper.NotificationDtoMapper;
import liaison.groble.application.notification.service.NotificationService;

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
}
