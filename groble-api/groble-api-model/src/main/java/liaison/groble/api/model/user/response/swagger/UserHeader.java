package liaison.groble.api.model.user.response.swagger;

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
@Operation(
    summary = "홈화면 헤더 정보 조회",
    description = "홈화면에 표시될 사용자 헤더 정보를 조회합니다. 로그인 상태 여부에 관계없이 응답을 반환합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UserHeaderApiResponse.class))),
  @ApiResponse(responseCode = "500", description = "서버 오류")
})
public @interface UserHeader {}
