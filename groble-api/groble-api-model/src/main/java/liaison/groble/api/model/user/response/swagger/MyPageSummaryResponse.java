package liaison.groble.api.model.user.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;

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
      description = "요청 성공",
      content = {
        @Content(
            mediaType = "application/json",
            schema =
                @Schema(
                    oneOf = {BuyerMyPageSummaryResponse.class, SellerMyPageSummaryResponse.class},
                    discriminatorProperty = "userType.code"),
            examples = {
              @ExampleObject(
                  name = "구매자 응답",
                  summary = "구매자 마이페이지 요약 정보",
                  value =
                      """
                  {
                    "status": "SUCCESS",
                    "code": 200,
                    "message": "요청이 성공적으로 처리되었습니다.",
                    "data": {
                      "nickname": "권동민",
                      "profileImageUrl": null,
                      "userType": {
                        "code": "BUYER",
                        "description": "구매자"
                      },
                      "canSwitchToSeller": false
                    },
                    "timestamp": "2025-05-06 04:26:26"
                  }
                  """),
              @ExampleObject(
                  name = "판매자 응답",
                  summary = "판매자 마이페이지 요약 정보",
                  value =
                      """
                  {
                    "status": "SUCCESS",
                    "code": 200,
                    "message": "요청이 성공적으로 처리되었습니다.",
                    "data": {
                      "nickname": "김판매",
                      "profileImageUrl": "https://example.com/profile.jpg",
                      "userType": {
                        "code": "SELLER",
                        "description": "판매자"
                      },
                      "verificationStatus": {
                        "code": "APPROVED",
                        "description": "승인됨"
                      }
                    },
                    "timestamp": "2025-05-06 04:26:26"
                  }
                  """)
            })
      }),
  @ApiResponse(responseCode = "401", description = "인증 실패"),
  @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
})
public @interface MyPageSummaryResponse {}
