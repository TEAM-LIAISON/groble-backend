package liaison.groble.api.server.auth.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public final class AccountVerificationSwaggerDocs {
  private AccountVerificationSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Auth.VERIFICATION;
  public static final String TAG_DESCRIPTION = SwaggerTags.Auth.VERIFICATION_DESC;

  // === 개인 메이커 계좌 인증 API ===
  public static final String PERSONAL_MAKER_VERIFICATION_SUMMARY =
      "[✅ 개인 메이커 인증] 개인 메이커 사용자의 계좌, 통장 사본 정보를 인증 요청합니다.";
  public static final String PERSONAL_MAKER_VERIFICATION_DESCRIPTION =
      """
                개인 메이커 사용자의 계좌, 통장 사본 정보를 인증 요청합니다.

                **주의사항:**
                - 인증 요청 후 관리자의 승인이 필요합니다.
                - 인증이 완료되면 정산이 가능합니다.
                """;

  // === 개인 • 법인 사업자 메이커 계좌 및 통장 사본 저장 요청 API ===
  public static final String BUSINESS_MAKER_BANKBOOK_VERIFICATION_SUMMARY =
      "[✅ 개인 • 법인 사업자 메이커 계좌 및 통장 사본 저장 요청] 개인 • 법인 사업자 메이커 사용자의 계좌, 통장 사본 정보를 저장 요청합니다.";
  public static final String BUSINESS_MAKER_BANKBOOK_VERIFICATION_DESCRIPTION =
      """
                개인 • 법인 사업자 메이커 사용자의 계좌, 통장 사본 정보를 저장 요청합니다.

                **주의사항:**
                - 저장 요청 후 관리자의 승인이 필요합니다.
                - 인증이 완료되면 정산이 가능합니다.
                """;

  // === 개인 • 법인 사업자 메이커 인증 API ===
  public static final String BUSINESS_MAKER_VERIFICATION_SUMMARY =
      "[✅ 개인 • 법인 사업자 메이커 인증] 개인 • 법인 사업자 메이커 사용자의 사업자 등록증 정보를 인증 요청합니다.";
  public static final String BUSINESS_MAKER_VERIFICATION_DESCRIPTION =
      """
                개인 • 법인 사업자 메이커 사용자의 사업자 등록증 정보를 인증 요청합니다.

                **주의사항:**
                - 인증 요청 후 관리자의 승인이 필요합니다.
                - 인증이 완료되면 정산이 가능합니다.
                """;
}
