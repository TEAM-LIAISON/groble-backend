package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public final class AdminDashboardSwaggerDocs {
  private AdminDashboardSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Admin.DASHBOARD;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.DASHBOARD_DESC;

  // === 대시보드 개요 조회 API ===
  public static final String DASHBOARD_OVERVIEW_SUMMARY = "[✅ 대시보드 개요 조회] 관리자가 대시보드 개요 정보를 조회합니다.";
  public static final String DASHBOARD_OVERVIEW_DESCRIPTION =
      """
                      관리자가 대시보드 개요 정보를 조회합니다.

                      **응답 데이터:**
                      - 주요 통계 정보 포함 (총 사용자 수, 총 콘텐츠 수, 총 매출 등)
                      """;

  public static final String DASHBOARD_TREND_SUMMARY = "[📈 대시보드 추세 조회] 관리자가 기간별 거래 추세 데이터를 조회합니다.";
  public static final String DASHBOARD_TREND_DESCRIPTION =
      """
                      관리자가 특정 기간의 일자별 거래 지표(매출, 건수, 객단가)를 조회합니다.

                      **응답 데이터:**
                      - 요청 기간 (startDate, endDate)
                      - 전체 매출/건수/평균 객단가
                      - 일자별 상세 지표 목록
                      """;

  public static final String DASHBOARD_TOP_CONTENTS_SUMMARY =
      "[🔥 인기 콘텐츠 조회] 관리자가 기간 내 상위 판매 콘텐츠를 조회합니다.";
  public static final String DASHBOARD_TOP_CONTENTS_DESCRIPTION =
      """
                      관리자가 기간 내 매출 기준 상위 콘텐츠 목록을 조회합니다.

                      **응답 데이터:**
                      - 요청 기간 및 조회 개수(limit)
                      - 콘텐츠 정보(콘텐츠 ID, 타이틀, 메이커, 매출, 판매건수, 객단가)
                      """;
}
