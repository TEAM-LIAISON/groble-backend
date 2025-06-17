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

/** 관리자 주문 요약 조회 엔드포인트용 Swagger 애노테이션 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "현재까지 주문 목록들에 대한 조회", description = "결제 완료, 결제 취소 요청, 환불 완료 등에 대한 주문들을 조회합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AdminOrderSummaryInfoApiResponse.class),
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
                        "createdAt": "2025-06-06 15:23:08",
                        "contentType": null,
                        "purchaserName": "동민통합local",
                        "contentTitle": null,
                        "finalPrice": 0.00,
                        "orderStatus": "PAID"
                      },
                      {
                        "createdAt": "2025-06-04 18:28:07",
                        "contentType": null,
                        "purchaserName": "테스트",
                        "contentTitle": null,
                        "finalPrice": 195000.00,
                        "orderStatus": "PAID"
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
                  "timestamp": "2025-06-12 17:00:55"
                }
              """)))
})
public @interface AdminOrderSummaryInfo {}
