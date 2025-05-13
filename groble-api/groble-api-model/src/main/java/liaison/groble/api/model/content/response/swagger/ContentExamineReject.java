package liaison.groble.api.model.content.response.swagger;

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
@Operation(summary = "콘텐츠 심사 거절 사유 조회", description = "심사 거절된 콘텐츠의 거절 사유를 조회합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "콘텐츠 심사 거절 사유 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentExamineRejectApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "거절 사유 조회 성공",
                    summary = "거절 사유가 존재하는 경우",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "콘텐츠 심사 거절 사유 조회 성공",
                                                  "data": "콘텐츠 내용이 서비스 이용 규정에 위배됩니다. 이미지 품질이 낮고 제공하는 정보가 불충분합니다.",
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "거절 사유 없음",
                    summary = "거절 사유가 없는 경우",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "콘텐츠 심사 거절 사유 조회 성공",
                                                  "data": "",
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 또는 심사 거절 상태가 아닌 콘텐츠",
      content =
          @Content(
              mediaType = "application/json",
              examples = {
                @ExampleObject(
                    value =
                        """
                                                {
                                                  "status": "ERROR",
                                                  "code": 400,
                                                  "message": "해당 콘텐츠는 심사 거절 상태가 아닙니다.",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(
      responseCode = "404",
      description = "해당 ID의 콘텐츠를 찾을 수 없음",
      content =
          @Content(
              mediaType = "application/json",
              examples = {
                @ExampleObject(
                    value =
                        """
                                                {
                                                  "status": "ERROR",
                                                  "code": 404,
                                                  "message": "해당 콘텐츠를 찾을 수 없습니다.",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "403", description = "권한 없음 (콘텐츠 소유자 또는 관리자만 조회 가능)")
})
public @interface ContentExamineReject {}
