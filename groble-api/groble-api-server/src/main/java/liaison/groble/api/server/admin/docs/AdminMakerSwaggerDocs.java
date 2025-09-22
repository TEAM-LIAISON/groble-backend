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
}
