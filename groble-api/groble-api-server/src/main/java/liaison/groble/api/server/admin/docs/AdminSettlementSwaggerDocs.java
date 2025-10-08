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
                    - 페이플 응답 메모(settlementNote) 포함
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

  public static final String SETTLEMENT_MANUAL_COMPLETE_SUMMARY =
      "[✅ 정산 수동 완료 처리] 관리자가 정산 상태를 완료로 지정하고 완료 일시/메모를 기록합니다.";
  public static final String SETTLEMENT_MANUAL_COMPLETE_DESCRIPTION =
      """
                      관리자가 페이플 자동 처리를 거치지 않고 정산을 즉시 완료 상태로 변경합니다.

                      **처리 내용:**
                      - 완료 시각(settledAt)은 서버에서 현재 시간으로 설정합니다.
                      - 정산 메모는 성공 여부에 따라 서버가 자동으로 기록합니다.

                      **응답 데이터:**
                      - 업데이트된 정산 상세 정보
                      """;

  public static final String PG_FEE_ADJUSTMENTS_SUMMARY =
      "[✅ PG 수수료 차액 내역 조회] 관리자가 PG 2.9%와 기준 1.7% 간 차액을 확인합니다.";
  public static final String PG_FEE_ADJUSTMENTS_DESCRIPTION =
      """
                      관리자가 PG 실제 청구 수수료(2.9%)와 표시 기준(1.7%) 간 차액을 거래 단위로 조회합니다.

                      **요청 파라미터:**
                      - startDate, endDate: 조회 기간 (선택)
                      - settlementId: 특정 정산 묶음 필터 (선택)
                      - sellerId: 판매자 필터 (선택)

                      **응답 데이터:**
                      - 각 거래별 PG 수수료 차액, VAT 차액, 환급 예상액 정보
                      - 페이징 정보 포함
                      """;
}
