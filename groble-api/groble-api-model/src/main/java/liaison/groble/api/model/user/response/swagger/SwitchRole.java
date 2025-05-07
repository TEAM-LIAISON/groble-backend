package liaison.groble.api.model.user.response.swagger;

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
@Operation(summary = "가입 유형 전환", description = "판매자 또는 구매자로 가입 유형을 전환합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "204",
      description = "역할 전환 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UserSwitchRoleApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "역할 전환 성공",
                    summary = "가입 유형 전환 성공",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 204,
                                                  "message": "가입 유형이 전환되었습니다.",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(
      responseCode = "400",
      description = "역할 전환 실패 - 해당 역할이 할당되어 있지 않음",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UserSwitchRoleApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "역할 전환 실패",
                    summary = "역할 할당 없음",
                    value =
                        """
                                                {
                                                  "status": "ERROR",
                                                  "code": 400,
                                                  "message": "해당 역할이 할당되어 있지 않습니다.",
                                                  "data": null,
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "403", description = "권한 없음")
})
public @interface SwitchRole {}
