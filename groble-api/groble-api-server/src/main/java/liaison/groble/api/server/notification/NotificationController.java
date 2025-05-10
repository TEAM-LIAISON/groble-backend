package liaison.groble.api.server.notification;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림 관련 API", description = "알림 삭제, 알림 조회 API")
public class NotificationController {}
