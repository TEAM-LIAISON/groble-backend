package liaison.groble.api.model.notification.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "알림 목록 정보 조회", description = "사용자가 알림 목록 정보를 조회합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NotificationItemsApiResponse.class))),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "알림 목록 정보를 찾을 수 없음")
})
public @interface UserNotifications {}
