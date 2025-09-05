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
}
