package liaison.groble.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "요청 성공",
      content = @Content(schema = @Schema(ref = "#/components/schemas/GrobleResponseObject"))),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청",
      content = @Content(schema = @Schema(ref = "#/components/schemas/GrobleResponse"))),
  @ApiResponse(
      responseCode = "401",
      description = "인증 실패",
      content = @Content(schema = @Schema(ref = "#/components/schemas/GrobleResponse"))),
  @ApiResponse(
      responseCode = "403",
      description = "접근 권한 없음",
      content = @Content(schema = @Schema(ref = "#/components/schemas/GrobleResponse"))),
  @ApiResponse(
      responseCode = "404",
      description = "리소스 없음",
      content = @Content(schema = @Schema(ref = "#/components/schemas/GrobleResponse"))),
  @ApiResponse(
      responseCode = "500",
      description = "서버 오류",
      content = @Content(schema = @Schema(ref = "#/components/schemas/GrobleResponse")))
})
public @interface GrobleApiResponse {}
