package liaison.groble.api.model.admin.response.swagger;

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
@Operation(summary = "[✅ 관리자 사용자] 전체 사용자 조회")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AdminUserSummaryInfoApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "success",
                      summary = "표준 성공 응답",
                      value =
                          """
                {
                  "status": "SUCCESS",
                  "code": 200,
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "items": [
                      {
                        "createdAt": "2025-06-06 15:09:47",
                        "nickname": "동민통합local",
                        "email": "kwondm7@naver.com",
                        "phoneNumber": "010-3661-4067",
                        "userStatus": "ACTIVE",
                        "verificationStatus": "PENDING",
                        "businessSeller": false,
                        "businessType": "individual-simple",
                        "sellerTermsAgreed": true,
                        "marketingAgreed": true,
                        "sellerInfo": false,
                        "withdrawalReason": null
                      },
                      {
                        "createdAt": "2025-06-02 17:13:49",
                        "nickname": "테스트",
                        "email": "kwondm7@naver.com",
                        "phoneNumber": "010-3661-4067",
                        "userStatus": "PENDING_VERIFICATION",
                        "verificationStatus": "VERIFIED",
                        "businessSeller": true,
                        "businessType": "corporation",
                        "sellerTermsAgreed": true,
                        "marketingAgreed": true,
                        "sellerInfo": true,
                        "withdrawalReason": null
                      },
                      {
                        "createdAt": "2025-06-02 15:16:18",
                        "nickname": "testuser",
                        "email": "test@example.com1",
                        "phoneNumber": "010-1111-1111",
                        "userStatus": "WITHDRAWN",
                        "verificationStatus": "VERIFIED",
                        "businessSeller": false,
                        "businessType": "none",
                        "sellerTermsAgreed": false,
                        "marketingAgreed": false,
                        "sellerInfo": false,
                        "withdrawalReason": "서비스를 잘 이용하지 않아요 - 더 나은 옵션을 찾았어요"
                      }
                    ],
                    "pageInfo": {
                      "currentPage": 0,
                      "totalPages": 1,
                      "pageSize": 12,
                      "totalElements": 3,
                      "first": true,
                      "last": true,
                      "empty": false
                    },
                    "meta": {
                      "sortBy": "createdAt",
                      "sortDirection": "DESC"
                    }
                  },
                  "timestamp": "2025-06-10 14:05:43"
                }
                """)))
})
public @interface AdminUserSummaryInfo {}
