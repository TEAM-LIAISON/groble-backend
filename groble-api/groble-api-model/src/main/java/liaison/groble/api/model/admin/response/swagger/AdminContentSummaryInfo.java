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

/** 관리자 콘텐츠 요약 조회 엔드포인트용 Swagger 애노테이션 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "전체 콘텐츠 조회")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AdminContentSummaryInfoApiResponse.class),
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
                        "contentId": 1,
                        "createdAt": "2025-06-06 15:09:47",
                        "contentType": "DOCUMENT",
                        "sellerName": "홍길동",
                        "contentTitle": "자바 프로그래밍 입문",
                        "priceOptionLength": 3,
                        "minPrice": 100000,
                        "contentStatus": "ACTIVE",
                        "adminContentCheckingStatus": "PENDING"
                      },
                      {
                        "contentId": 2,
                        "createdAt": "2025-06-04 11:22:30",
                        "contentType": "COACHING",
                        "sellerName": "김철수",
                        "contentTitle": "실전 알고리즘 튜터링",
                        "priceOptionLength": 2,
                        "minPrice": 200000,
                        "contentStatus": "DRAFT",
                        "adminContentCheckingStatus": "DISCONTINUED"
                      }
                    ],
                    "pageInfo": {
                      "currentPage": 0,
                      "totalPages": 1,
                      "pageSize": 12,
                      "totalElements": 2,
                      "first": true,
                      "last": true,
                      "empty": false
                    },
                    "meta": {
                      "sortBy": "createdAt",
                      "sortDirection": "DESC"
                    }
                  },
                  "timestamp": "2025-06-12 17:10:00"
                }
              """)))
})
public @interface AdminContentSummaryInfo {}
