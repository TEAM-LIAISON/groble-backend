package liaison.groble.api.model.auth.response.swagger;

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
@Operation(summary = "회원가입", description = "새로운 사용자를 등록하고 인증 토큰을 발급합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "회원가입 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SignUpApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "통합 회원가입 성공 응답",
                    summary = "통합 회원가입 성공",
                    value =
                        """
                            {
                                "status": "SUCCESS",
                                "code": 201,
                                "message": "회원가입이 성공적으로 완료되었습니다.",
                                "data": {
                                    "email": "email@email.com",
                                    "authenticated": true
                                },
                                "timestamp": "2025-05-07 23:00:44"
                            }
                            """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 손상된 경우")
})
public @interface SignUp {}
