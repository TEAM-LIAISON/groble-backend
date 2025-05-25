// package liaison.groble.api.server.notification;
//
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import liaison.groble.api.model.notification.response.NotificationItems;
// import liaison.groble.api.model.notification.response.swagger.UserNotifications;
// import liaison.groble.api.server.notification.mapper.NotificationDtoMapper;
// import liaison.groble.application.notification.dto.NotificationItemsDto;
// import liaison.groble.application.notification.service.NotificationService;
// import liaison.groble.common.annotation.Auth;
// import liaison.groble.common.model.Accessor;
// import liaison.groble.common.response.GrobleResponse;
//
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;
//
/// **
// * @deprecated OpenAPI Generator 마이그레이션 완료 후 삭제 예정
// * @see NotificationApiController 새로운 구현체 참조
// */
// @Deprecated
// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/api/v1/notifications")
// @Tag(name = "알림 관련 API", description = "알림 삭제, 알림 조회 API")
// public class NotificationController {
//
//  private final NotificationService notificationService;
//  private final NotificationDtoMapper notificationDtoMapper;
//
//  // 알림 전체 삭제
//  @Operation(summary = "알림 전체 삭제", description = "사용자의 모든 알림을 삭제합니다.")
//  @DeleteMapping
//  public ResponseEntity<GrobleResponse<Void>> deleteAllNotifications(@Auth Accessor accessor) {
//    notificationService.deleteAllNotifications(accessor.getUserId());
//    return ResponseEntity.ok(GrobleResponse.success(null, "모든 알림이 삭제되었습니다."));
//  }
//
//  // 알림 단일 삭제
//  @Operation(summary = "알림 단일 삭제", description = "특정 알림을 삭제합니다.")
//  @DeleteMapping("/{notificationId}")
//  public ResponseEntity<GrobleResponse<Void>> deleteNotification(
//      @Auth Accessor accessor, @PathVariable Long notificationId) {
//    notificationService.deleteNotification(accessor.getUserId(), notificationId);
//    return ResponseEntity.ok(GrobleResponse.success(null, "알림이 삭제되었습니다."));
//  }
//
//  // 알림 전체 조회
//  @UserNotifications
//  @GetMapping
//  public ResponseEntity<GrobleResponse<NotificationItems>> getNotifications(
//      @Auth Accessor accessor) {
//    NotificationItemsDto notificationItemsDto =
//        notificationService.getNotificationItems(accessor.getUserId());
//    NotificationItems notificationItems =
//        notificationDtoMapper.toNotificationItems(notificationItemsDto);
//    return ResponseEntity.ok(GrobleResponse.success(notificationItems, "알림 조회 성공"));
//  }
// }
