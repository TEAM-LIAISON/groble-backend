package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/** 관리자 인증/인가 API 명세 상수 모음 */
public final class AdminAuthSwaggerDocs {

  private AdminAuthSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Admin.AUTH;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.AUTH_DESC;

  // === 관리자 로그인 ===
  public static final String SIGN_IN_SUMMARY = "[✅ 관리자 로그인] 관리자 계정으로 로그인";
  public static final String SIGN_IN_DESCRIPTION = "관리자 계정 자격 증명을 검증하고, 액세스/리프레시 토큰을 발급합니다.";

  // === 관리자 로그아웃 ===
  public static final String LOGOUT_SUMMARY = "[✅ 관리자 로그아웃] 관리자 계정 로그아웃";
  public static final String LOGOUT_DESCRIPTION = "관리자 인증 쿠키를 제거하여 세션을 만료 처리합니다.";
}
