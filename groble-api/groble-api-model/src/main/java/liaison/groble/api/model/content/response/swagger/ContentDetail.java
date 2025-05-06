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
@Operation(summary = "콘텐츠 상세 정보 조회", description = "콘텐츠 상세 정보를 조회합니다. [코칭 & 자료]")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "콘텐츠 상세 정보 조회 성공",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContentDetail.class),
            examples = {
              @ExampleObject(
                  name = "콘텐츠 상세 정보 응답",
                  summary = "콘텐츠 상세 정보",
                  value =
                      """
                                                      {
                                                        "status": "SUCCESS",
                                                        "code": 200,
                                                        "message": "요청이 성공적으로 처리되었습니다.",
                                                        "data": {
                                                          "nickname": "권동민",
                                                          "accountType": {
                                                            "code": "INTEGRATED",
                                                            "description": "통합 계정"
                                                          },
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
  @ApiResponse(responseCode = "404", description = "콘텐츠 정보를 찾을 수 없음")
})
public @interface ContentDetail {}
