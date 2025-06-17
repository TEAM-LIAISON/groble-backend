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
@Operation(summary = "소셜 가입 기본 정보 설정", description = "소셜 로그인 과정에서 사용자의 기본 정보를 설정합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "소셜 가입 기본 정보 설정 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SetSocialBasicInfoApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "소셜 가입 기본 정보 설정 성공 예시",
                    summary = "소셜 가입 기본 정보 설정 성공 예시",
                    value =
                        """
                {
                    "status": "SUCCESS",
                    "code": 200,
                    "message": "요청이 성공적으로 처리되었습니다.",
                    "data": {
                        "소셜 계정 기본 정보가 성공적으로 설정되었습니다."
                    },
                    "timestamp": "2025-05-07 23:00:44"
                }
                """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 손상된 경우")
})
public @interface SetSocialBasicInfo {}
