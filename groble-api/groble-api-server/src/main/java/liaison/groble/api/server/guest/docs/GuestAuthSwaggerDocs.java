package liaison.groble.api.server.guest.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/** 게스트 인증 API Swagger 문서 상수 클래스 */
public class GuestAuthSwaggerDocs {
  private GuestAuthSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Guest.AUTH;
  public static final String TAG_DESCRIPTION = SwaggerTags.Guest.AUTH_DESC;

  // === 비회원 전화번호 인증 코드 요청 API ===
  public static final String CODE_REQUEST_SUMMARY =
      "[✅ 비회원 전화번호 인증 코드 발송 요청] 기입한 전화번호로 인증 코드를 전송합니다.";
  public static final String CODE_REQUEST_DESCRIPTION =
      """
            사용자가 기입한 전화번호로 인증 코드를 전송합니다.

            **주의사항:**
            - 인증 코드는 5분간 유효합니다.
            """;

  // === 비회원 전화번호 인증 코드 검증 API ===
  public static final String CODE_VERIFY_SUMMARY =
      "[✅ 비회원 전화번호 인증 코드 검증 및 토큰 발급] 기입한 인증 코드를 검증하고 비회원 토큰을 발급합니다.";
  public static final String CODE_VERIFY_DESCRIPTION =
      """
              사용자가 기입한 인증 코드를 검증하고, 성공 시 비회원 토큰을 발급합니다.

              **주의사항:**
              - 인증 코드는 5분간 유효합니다.
              - 인증 코드는 6자리 숫자여야 합니다.
              - 인증 실패 시 400 에러가 발생합니다.
              - 발급된 비회원 토큰은 쿠키에 저장됩니다.
              """;
}
