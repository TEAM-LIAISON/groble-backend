package liaison.groble.api.model.notification.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
              schema = @Schema(implementation = NotificationItemsApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "여러 알림 타입 혼합",
                    summary = "여러 알림 타입이 혼합된 예제",
                    description = "시스템 환영 알림, 판매자 인증 알림, 콘텐츠 승인/거부 알림 등 여러 유형의 알림이 혼합된 예제",
                    value = NotificationExamples.MixedNotificationsExample.EXAMPLE),
                @ExampleObject(
                    name = "판매자 인증 성공",
                    summary = "판매자 인증 성공 알림 예제",
                    description = "판매자가 인증에 성공했을 때 받는 알림",
                    value = NotificationExamples.SellerVerifiedExample.EXAMPLE),
                @ExampleObject(
                    name = "판매자 인증 거부",
                    summary = "판매자 인증 거부 알림 예제",
                    description = "판매자 인증이 거부되었을 때 받는 알림",
                    value = NotificationExamples.SellerRejectedExample.EXAMPLE),
                @ExampleObject(
                    name = "콘텐츠 승인",
                    summary = "콘텐츠 승인 알림 예제",
                    description = "업로드한 콘텐츠가 승인되었을 때 받는 알림",
                    value = NotificationExamples.ContentApprovedExample.EXAMPLE),
                @ExampleObject(
                    name = "콘텐츠 거부",
                    summary = "콘텐츠 거부 알림 예제",
                    description = "업로드한 콘텐츠가 거부되었을 때 받는 알림",
                    value = NotificationExamples.ContentRejectedExample.EXAMPLE),
                @ExampleObject(
                    name = "시스템 환영",
                    summary = "시스템 환영 알림 예제",
                    description = "신규 사용자가 가입 시 받는 환영 알림",
                    value = NotificationExamples.WelcomeGrobleExample.EXAMPLE)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "알림 목록 정보를 찾을 수 없음")
})
public @interface UserNotifications {}
