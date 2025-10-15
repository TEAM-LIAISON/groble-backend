package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/** 관리자 콘텐츠 관리 API 명세 상수 모음 */
public final class AdminContentSwaggerDocs {

  private AdminContentSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Admin.CONTENT;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.CONTENT_DESC;

  // === 콘텐츠 목록 조회 ===
  public static final String GET_ALL_CONTENTS_SUMMARY = "[✅ 관리자 콘텐츠] 전체 콘텐츠 목록 조회";
  public static final String GET_ALL_CONTENTS_DESCRIPTION =
      """
          관리자 전용 페이지에서 콘텐츠 요약 정보를 페이징으로 조회합니다.
          \n- 콘텐츠 상태, 가격 옵션, 판매자 정보 등의 요약 데이터를 제공합니다.
          """;

  // === 콘텐츠 심사 처리 ===
  public static final String EXAMINE_CONTENT_SUMMARY = "[✅ 관리자 콘텐츠] 콘텐츠 심사 처리";
  public static final String EXAMINE_CONTENT_DESCRIPTION =
      """
          관리자 권한으로 콘텐츠 심사를 승인하거나 반려합니다.
          \n- 승인 시 콘텐츠는 판매 가능 상태로 전환됩니다.
          - 반려 시 반려 사유와 함께 심사 상태가 업데이트됩니다.
          """;

  // === 메이커 대시보드 모니터링 ===
  public static final String MAKER_DASHBOARD_CONTENTS_SUMMARY = "[✅ 관리자 콘텐츠] 메이커 대시보드 콘텐츠 목록 조회";
  public static final String MAKER_DASHBOARD_CONTENTS_DESCRIPTION =
      """
          marketLinkUrl을 기준으로 메이커 대시보드에 노출되는 콘텐츠 목록을 조회합니다.
          \n- 관리자 페이지에서 동일한 정렬/페이징 규칙으로 모니터링할 수 있습니다.
          """;

  public static final String MAKER_DASHBOARD_CONTENT_VIEW_STATS_SUMMARY =
      "[✅ 관리자 콘텐츠] 메이커 대시보드 콘텐츠 조회수 통계 조회";
  public static final String MAKER_DASHBOARD_CONTENT_VIEW_STATS_DESCRIPTION =
      """
          지정한 기간 동안의 콘텐츠별 조회수 합계를 조회합니다.
          \n- TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH, LAST_MONTH 구간을 지원합니다.
          """;
}
