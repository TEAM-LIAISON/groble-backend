package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

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
}
