package liaison.groble.api.server.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import liaison.groble.api.model.notification.response.NotificationItems;
import liaison.groble.api.model.notification.response.swagger.UserNotifications;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** NotificationApi 인터페이스 OpenAPI Generator가 활성화되면 이 인터페이스는 자동 생성됩니다. 현재는 마이그레이션을 위한 임시 인터페이스입니다. */
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림 관련 API", description = "알림 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApi {

  @Operation(summary = "알림 전체 삭제", description = "사용자의 모든 알림을 삭제합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "모든 알림이 삭제되었습니다.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GrobleResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @DeleteMapping
  ResponseEntity<GrobleResponse<Void>> deleteAllNotifications(
      @Parameter(hidden = true) @Auth Accessor accessor);

  @Operation(summary = "알림 단일 삭제", description = "특정 알림을 삭제합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "알림이 삭제되었습니다.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GrobleResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @DeleteMapping("/{notificationId}")
  ResponseEntity<GrobleResponse<Void>> deleteNotification(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @Parameter(description = "삭제할 알림 ID", required = true) @PathVariable Long notificationId);

  @Operation(summary = "알림 전체 조회", description = "사용자의 모든 알림을 조회합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "알림 조회 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationItems.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @UserNotifications
  @GetMapping
  ResponseEntity<GrobleResponse<NotificationItems>> getNotifications(
      @Parameter(hidden = true) @Auth Accessor accessor);
}
