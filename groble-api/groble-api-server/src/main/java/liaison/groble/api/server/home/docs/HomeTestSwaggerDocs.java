package liaison.groble.api.server.home.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public final class HomeTestSwaggerDocs {

  private HomeTestSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.HomeTest.PHONE_AUTH;
  public static final String TAG_DESCRIPTION = SwaggerTags.HomeTest.PHONE_AUTH_DESC;

  // === 홈 테스트 전화 인증 코드 발송 ===
  public static final String SEND_CODE_SUMMARY = "[📨 홈 테스트] 전화번호 인증 코드 발송";
  public static final String SEND_CODE_DESCRIPTION =
      "홈 테스트 환경에서 입력한 전화번호로 인증 코드를 발송합니다.<br/>" + "- 요청 성공 시, 발송된 인증 코드 정보 및 대기 시간을 응답합니다.";

  // === 홈 테스트 전화 인증 코드 검증 ===
  public static final String VERIFY_CODE_SUMMARY = "[✅ 홈 테스트] 전화번호 인증 코드 검증";
  public static final String VERIFY_CODE_DESCRIPTION =
      "이전에 발송된 인증 코드를 검증하여 홈 테스트 인증 절차를 진행합니다.<br/>"
          + "- 요청 성공 시, 이후 이메일 저장 및 플로우 완료 단계에서 사용할 `verificationToken`을 응답합니다.";

  // === 홈 테스트 이메일 저장 ===
  public static final String SAVE_EMAIL_SUMMARY = "[📧 홈 테스트] 이메일 정보 저장";
  public static final String SAVE_EMAIL_DESCRIPTION =
      "전화번호 인증 단계에서 발급받은 `verificationToken`을 사용하여 이메일 정보를 저장합니다.<br/>"
          + "- 요청 성공 시, 저장된 이메일 정보와 함께 갱신된 인증 정보를 응답합니다.";

  // === 홈 테스트 플로우 완료 ===
  public static final String COMPLETE_FLOW_SUMMARY = "[🏁 홈 테스트] 인증 플로우 완료 처리";
  public static final String COMPLETE_FLOW_DESCRIPTION =
      "verify 단계에서 발급받은 `verificationToken`을 사용해 홈 테스트 플로우를 최종 완료 처리합니다.<br/>"
          + "- 요청 성공 시, 검증된 사용자 정보와 함께 완료 결과를 응답합니다.";
}
