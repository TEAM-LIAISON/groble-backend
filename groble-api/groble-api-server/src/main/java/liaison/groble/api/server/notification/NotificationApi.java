package liaison.groble.api.server.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import liaison.groble.api.model.notification.response.NotificationItemsResponse;
import liaison.groble.api.model.notification.response.swagger.NotificationExamples;
import liaison.groble.api.model.notification.response.swagger.NotificationItemsApiResponse;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** NotificationApi 인터페이스 OpenAPI Generator가 활성화되면 이 인터페이스는 자동 생성됩니다. 현재는 마이그레이션을 위한 임시 인터페이스입니다. */
@RequestMapping("/api/v1/notifications")
@Tag(name = "[🔔 알림] 알림 삭제/조회", description = "알림 삭제 및 조회 API")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApi {

  @Operation(summary = "[✅ 알림 전체 삭제]", description = "사용자의 모든 알림을 삭제합니다.")
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

  @Operation(summary = "[✅ 알림 단일 삭제]", description = "특정 알림을 삭제합니다.")
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

  @Operation(summary = "[✅ 알림 전체 조회]", description = "사용자의 모든 알림을 조회합니다.")
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
                      name = "콘텐츠 구매",
                      summary = "콘텐츠 구매 알림 예제",
                      description = "사용자가 상품을 구매했을 때 받는 알림",
                      value = NotificationExamples.ContentPurchasedExample.EXAMPLE),
                  @ExampleObject(
                      name = "리뷰 답글",
                      summary = "콘텐츠 리뷰 답글 알림 예제",
                      description = "사용자의 리뷰에 답글이 달렸을 때 받는 알림",
                      value = NotificationExamples.ContentReviewReplyExample.EXAMPLE),
                  @ExampleObject(
                      name = "리뷰 등록",
                      summary = "리뷰 등록 알림 예제",
                      description = "사용자가 콘텐츠에 리뷰를 등록했을 때 받는 알림",
                      value = NotificationExamples.ContentReviewedExample.EXAMPLE),
                  @ExampleObject(
                      name = "콘텐츠 판매",
                      summary = "상품 판매 알림 예제",
                      description = "메이커의 콘텐츠가 판매되었을 때 받는 알림",
                      value = NotificationExamples.ContentSoldExample.EXAMPLE),
                  @ExampleObject(
                      name = "판매 중단",
                      summary = "상품 판매 중단 알림 예제",
                      description = "메이커의 콘텐츠 판매가 중단되었을 때 받는 알림",
                      value = NotificationExamples.ContentSoldStoppedExample.EXAMPLE),
                  @ExampleObject(
                      name = "인증 성공",
                      summary = "메이커 인증 성공 알림 예제",
                      description = "메이커 인증에 성공했을 때 받는 알림",
                      value = NotificationExamples.MakerCertifiedExample.EXAMPLE),
                  @ExampleObject(
                      name = "인증 거부",
                      summary = "메이커 인증 거부 알림 예제",
                      description = "메이커 인증이 반려되었을 때 받는 알림",
                      value = NotificationExamples.MakerCertifyRejectedExample.EXAMPLE),
                  @ExampleObject(
                      name = "시스템 환영",
                      summary = "시스템 환영 알림 예제",
                      description = "신규 사용자가 가입 시 받는 환영 알림",
                      value = NotificationExamples.WelcomeGrobleExample.EXAMPLE),
                  @ExampleObject(
                      name = "혼합 알림",
                      summary = "여러 알림 타입이 혼합된 예제",
                      description = "모든 NotificationType과 SubNotificationType이 섞인 예제",
                      value = NotificationExamples.MixedNotificationsExample.EXAMPLE)
                })),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "404", description = "알림 목록 정보를 찾을 수 없음")
  })
  @GetMapping
  public ResponseEntity<GrobleResponse<NotificationItemsResponse>> getNotifications(
      @Parameter(hidden = true) @Auth Accessor accessor);
}
