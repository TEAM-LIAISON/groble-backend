package liaison.groble.application.user.service;

public interface UserService {
  /**
   * 사용자 기본 정보 업데이트
   *
   * @param userPk 사용자 식별자 (PK)
   * @param userName 사용자 이름 (닉네임)
   * @param userId 유저 아이디 (User ID)
   * @param ageConsent 만 14세 이상 동의
   * @param termsConsent 서비스 이용약관 동의
   * @param privacyConsent 개인정보 수집 및 이용 동의
   * @param marketingConsent 마케팅 정보 수신 동의
   */
  void updateUserBasicInfo(
      Long userPk,
      String userName,
      String userId,
      boolean ageConsent,
      boolean termsConsent,
      boolean privacyConsent,
      boolean marketingConsent);
}
