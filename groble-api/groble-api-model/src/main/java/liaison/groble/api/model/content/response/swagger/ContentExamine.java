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
@Operation(summary = "콘텐츠 심사 [관리자 기능]", description = "콘텐츠 승인 또는 반려 심사를 진행합니다. [관리자 기능]")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "콘텐츠 심사 처리 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentExamineApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "콘텐츠 심사 승인 성공",
                    summary = "콘텐츠 승인 성공",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "콘텐츠 심사 승인 성공",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "콘텐츠 심사 반려 성공",
                    summary = "콘텐츠 반려 성공",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "콘텐츠 심사 반려 성공",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(
      responseCode = "400",
      description = "심사 처리 실패 - 잘못된 요청 또는 지원하지 않는 액션",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentExamineApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "심사 처리 실패",
                    summary = "지원하지 않는 심사 액션",
                    value =
                        """
                                                {
                                                  "status": "ERROR",
                                                  "code": 400,
                                                  "message": "지원하지 않는 심사 액션입니다: INVALID_ACTION",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(
      responseCode = "404",
      description = "콘텐츠를 찾을 수 없음",
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
  @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 권한 필요)")
})
public @interface ContentExamine {}
