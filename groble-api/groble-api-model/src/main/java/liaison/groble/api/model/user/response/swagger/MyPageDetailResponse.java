package liaison.groble.api.model.user.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.common.swagger.SwaggerExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "마이페이지 요약 정보 조회",
    description = "마이페이지 첫 화면에서 요약 정보를 조회합니다. 사용자 유형(구매자/판매자)에 따라 응답 구조가 달라집니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "마이페이지 상세 정보 조회 성공",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserMyPageDetailResponse.class),
            examples = {
              @ExampleObject(
                  name = "상세 정보 응답",
                  summary = "마이페이지 상세 정보",
                  value = SwaggerExamples.USER_MYPAGE_DETAIL)
            })
      }),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
})
public @interface MyPageDetailResponse {}
