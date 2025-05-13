package liaison.groble.api.model.scrap.response.swagger;

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
    summary = "콘텐츠 스크랩 상태 변경",
    description = "콘텐츠 스크랩 상태 변경을 진행합니다. 스크랩 콘텐츠는 나의 스크랩 목록에서 확인할 수 있습니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "콘텐츠 스크랩 상태 변경 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UpdateContentScrapStateApiResponse.class))),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "콘텐츠 정보를 찾을 수 없음")
})
public @interface UpdateContentScrapState {}
