package liaison.groble.api.model.user.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.model.user.response.UserMyPageDetailResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "마이페이지 상세 정보 조회", description = "마이페이지에서 사용자 상세 정보를 조회합니다.")
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
                  value =
                      """
                    {
                      "status": "SUCCESS",
                      "code": 200,
                      "message": "요청이 성공적으로 처리되었습니다.",
                      "data": {
                        "nickname": "권동민",
                        "accountType": "INTEGRATED",
                        "providerType": null,
                        "email": "kwondm7@naver.com",
                        "profileImageUrl": "https://example.com/profile.jpg",
                        "phoneNumber": "010-1234-5678",
                        "sellerAccountNotCreated": true
                      },
                      "timestamp": "2025-05-06 04:26:26"
                    }
                    """)
            })
      }),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
})
public @interface MyPageDetail {}
