package liaison.groble.api.server.admin.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import liaison.groble.api.server.common.swagger.SwaggerTags;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/** 정산 API Swagger 문서 상수 클래스 */
public final class AdminSettlementSwaggerDocs {

  private AdminSettlementSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Admin.SETTLEMENT;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.SETTLEMENT_DESC;

  // === 모든 사용자의 정산 내역 조회 API ===
  public static final String ALL_USERS_SETTLEMENTS_SUMMARY =
      "[✅ 전체 사용자 정산 내역 조회] 관리자가 모든 사용자의 정산 내역을 페이징 처리하여 조회합니다.";
  public static final String ALL_USERS_SETTLEMENTS_DESCRIPTION =
      """
                    관리자가 모든 사용자의 정산 내역을 페이징 처리하여 조회합니다.

                    **응답 데이터:**
                    - 각 정산 항목에 대한 상세 정보 포함
                    - 페이징 정보 포함 (총 페이지 수, 현재 페이지 등)
                    """;

  // === 정산 상세 내역 조회 API ===
  public static final String SETTLEMENT_DETAIL_SUMMARY =
      "[✅ 정산 상세 내역 조회] 관리자가 특정 정산 항목의 상세 내역을 조회합니다.";
  public static final String SETTLEMENT_DETAIL_DESCRIPTION =
      """
                    관리자가 특정 정산 항목의 상세 내역을 조회합니다.

                    **요청 파라미터:**
                    - settlementId: 조회할 정산 항목의 고유 ID

                    **응답 데이터:**
                    - 정산 항목의 상세 정보 포함
                    - 관련된 결제 및 환불 내역 포함
                    """;
  // === 정산 상세 총 판매 내역 조회 API ===
  public static final String SETTLEMENT_DETAIL_SALES_SUMMARY =
      "[✅ 정산 상세 총 판매 내역 조회] 관리자가 특정 정산 항목의 총 판매 내역을 조회합니다.";
  public static final String SETTLEMENT_DETAIL_SALES_DESCRIPTION =
      """
                      관리자가 특정 정산 항목의 총 판매 내역을 조회합니다.

                      **요청 파라미터:**
                      - settlementId: 조회할 정산 항목의 고유 ID

                      **응답 데이터:**
                      - 해당 정산 항목에 포함된 모든 판매 내역 리스트
                      - 각 판매 내역에 대한 상세 정보 포함
                      """;

  @Operation(
      summary = "정산 승인 처리",
      description =
          """
          관리자가 선택된 정산 항목들을 일괄 승인하고, 페이플 정산을 실행합니다.

          **처리 플로우:**
          1. 정산 항목 조회 및 검증
          2. 개별 정산 항목 승인 처리
          3. 페이플 파트너 인증
          4. 페이플 계좌 인증
          5. 페이플 그룹 정산 요청

          **주의사항:**
          - 이미 완료된 정산 항목은 승인할 수 없습니다.
          - 페이플 정산 실행은 옵션이며, 실패 시에도 내부 승인은 완료됩니다.
          """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "정산 승인 요청 정보",
      content =
          @Content(
              mediaType = "application/json",
              examples =
                  @ExampleObject(
                      name = "정산 승인 요청 예시",
                      description = "5개 정산을 승인하는 요청",
                      value =
                          """
                          {
                            "settlementIds": [1, 2, 3, 4, 5],
                            "adminUserId": 12345,
                            "approvalReason": "월말 정산 승인 처리",
                            "executePaypleSettlement": true
                          }
                          """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "정산 승인 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      name = "전체 성공",
                      description = "모든 정산 항목이 성공적으로 승인된 경우",
                      value =
                          """
                          {
                            "success": true,
                            "message": "정산 승인이 완료되었습니다.",
                            "data": {
                              "success": true,
                              "approvedSettlementCount": 5,
                              "approvedItemCount": 15,
                              "totalApprovedAmount": 1500000,
                              "approvedAt": "2025-09-03T15:30:45",
                              "paypleResult": {
                                "success": true,
                                "responseCode": "T0000",
                                "responseMessage": "처리 성공",
                                "accessToken": "eyJhlNDlj...",
                                "expiresIn": "60"
                              },
                              "failedSettlements": null,
                              "excludedRefundedItemCount": 0
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "부분 실패",
                      description = "일부 정산 항목의 승인이 실패한 경우",
                      value =
                          """
                          {
                            "success": true,
                            "message": "정산 승인이 완료되었습니다.",
                            "data": {
                              "success": false,
                              "approvedSettlementCount": 3,
                              "approvedItemCount": 9,
                              "totalApprovedAmount": 900000,
                              "approvedAt": "2025-09-03T15:30:45",
                              "paypleResult": {
                                "success": true,
                                "responseCode": "T0000",
                                "responseMessage": "처리 성공",
                                "accessToken": "eyJhlNDlj...",
                                "expiresIn": "60"
                              },
                              "failedSettlements": [
                                {
                                  "settlementId": 4,
                                  "failureReason": "이미 완료된 정산입니다: 4"
                                },
                                {
                                  "settlementId": 5,
                                  "failureReason": "이미 완료된 정산입니다: 5"
                                }
                              ],
                              "excludedRefundedItemCount": 2
                            }
                          }
                          """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (유효성 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "message": "존재하지 않는 정산들: [999, 1000]",
                              "data": null
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "message": "정산 승인 처리 중 오류가 발생했습니다.",
                              "data": null
                            }
                            """)))
  })
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ApproveSettlements {}
}
