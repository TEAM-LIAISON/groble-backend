package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/** 관리자 메이커 관리 API 명세 상수 모음 */
public final class AdminMakerSwaggerDocs {

  private AdminMakerSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Admin.MAKER;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.MAKER_DESC;

  // === 메이커 상세 조회 ===
  public static final String GET_MAKER_DETAIL_SUMMARY = "[✅ 관리자 메이커] 메이커 상세 정보 조회";
  public static final String GET_MAKER_DETAIL_DESCRIPTION =
      """
          메이커 프로필, 콘텐츠 정보, 정산 계좌 등 상세 정보를 조회합니다.
          \n- 닉네임을 기준으로 메이커 정보를 탐색합니다.
          """;

  // === 메이커 인증 처리 ===
  public static final String VERIFY_MAKER_SUMMARY = "[✅ 관리자 메이커] 메이커 인증 처리";
  public static final String VERIFY_MAKER_DESCRIPTION =
      """
          메이커 인증 요청을 승인하거나 거절합니다.
          \n- 승인 시 메이커를 인증 상태로 전환합니다.
          - 거절 시 인증 거절 상태로 업데이트합니다.
          """;

  // === 관리자 메모 저장 ===
  public static final String SAVE_ADMIN_MEMO_SUMMARY = "[✅ 관리자 메이커] 관리자 메모 저장";
  public static final String SAVE_ADMIN_MEMO_DESCRIPTION =
      """
          특정 메이커에 대한 관리자 메모를 추가 또는 갱신합니다.
          \n- 기존 메모가 존재하면 덮어쓰고, 없으면 새로 생성합니다.
          """;

  // === 메이커 대시보드 모니터링 ===
  public static final String MAKER_DASHBOARD_OVERVIEW_SUMMARY = "[✅ 관리자 메이커] 메이커 대시보드 개요 조회";
  public static final String MAKER_DASHBOARD_OVERVIEW_DESCRIPTION =
      """
          marketLinkUrl을 기준으로 메이커 대시보드 개요 지표를 조회합니다.
          \n- 총 매출, 판매 건수, 검수 상태 등을 확인할 수 있습니다.
          """;

  public static final String MAKER_DASHBOARD_VIEW_STATS_SUMMARY = "[✅ 관리자 메이커] 메이커 대시보드 조회수 요약 조회";
  public static final String MAKER_DASHBOARD_VIEW_STATS_DESCRIPTION =
      """
          지정한 기간 동안의 마켓 및 콘텐츠 조회수 총합을 조회합니다.
          \n- TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH, LAST_MONTH 구간을 지원합니다.
          """;

  public static final String MAKER_DASHBOARD_MARKET_VIEW_STATS_SUMMARY =
      "[✅ 관리자 메이커] 메이커 대시보드 마켓 조회수 통계 조회";
  public static final String MAKER_DASHBOARD_MARKET_VIEW_STATS_DESCRIPTION =
      """
          기간별 마켓 조회수를 일자 단위로 조회합니다.
          \n- 조회 기간에 따라 일수만큼 페이지 크기가 동적으로 조정됩니다.
          """;

  public static final String MAKER_DASHBOARD_MARKET_REFERRER_STATS_SUMMARY =
      "[✅ 관리자 메이커] 메이커 대시보드 마켓 유입경로 조회";
  public static final String MAKER_DASHBOARD_MARKET_REFERRER_STATS_DESCRIPTION =
      """
          기간별 마켓 유입경로 통계를 조회합니다.
          \n- utm 정보와 도메인 정보를 포함한 방문 수를 확인할 수 있습니다.
          """;
}
