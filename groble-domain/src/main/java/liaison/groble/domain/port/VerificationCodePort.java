package liaison.groble.domain.port;

public interface VerificationCodePort {
  // === 이메일 인증 관련 ===
  void saveVerificationCode(String email, String code, long expirationTimeInMinutes);

  String getVerificationCode(String email);

  boolean validateVerificationCode(String email, String code);

  void removeVerificationCode(String email);

  void saveVerifiedFlag(String email, long expirationTimeInMinutes);

  boolean validateVerifiedFlag(String email);

  void removeVerifiedEmailFlag(String email);

  // === 비밀번호 재설정 관련 ===
  void savePasswordResetCode(String email, String token, long expirationTimeInMinutes);

  boolean validatePasswordResetCode(String token);

  String getPasswordResetEmail(String token);

  void removePasswordResetCode(String token);

  // === 로그인 사용자 전화번호 인증 관련 ===
  /** 로그인한 사용자의 전화번호 인증 코드 저장 Redis Key: phone:auth:user:{userId}:{phoneNumber} */
  void saveVerificationCodeForUser(
      Long userId, String phoneNumber, String code, long expirationTimeInMinutes);

  /** 로그인한 사용자의 전화번호 인증 코드 검증 */
  boolean validateVerificationCodeForUser(Long userId, String phoneNumber, String code);

  /** 로그인한 사용자의 전화번호 인증 코드 삭제 */
  void removeVerificationCodeForUser(Long userId, String phoneNumber);

  // === 비회원 전화번호 인증 관련 ===
  /** 비회원 전화번호 인증 코드 저장 Redis Key: phone:auth:guest:{phoneNumber} */
  void saveVerificationCodeForGuest(String phoneNumber, String code, long expirationTimeInMinutes);

  /** 비회원 전화번호 인증 코드 검증 */
  boolean validateVerificationCodeForGuest(String phoneNumber, String code);

  /** 비회원 전화번호 인증 코드 삭제 */
  void removeVerificationCodeForGuest(String phoneNumber);

  // === 홈 테스트 전화번호 인증 관련 ===
  /** 홈 테스트 전화번호 인증 코드 저장 Redis Key: phone:auth:home-test:{phoneNumber} */
  void saveVerificationCodeForHomeTest(String phoneNumber, String code, long expirationTimeInMinutes);

  /** 홈 테스트 전화번호 인증 코드 검증 */
  boolean validateVerificationCodeForHomeTest(String phoneNumber, String code);

  /** 홈 테스트 전화번호 인증 코드 삭제 */
  void removeVerificationCodeForHomeTest(String phoneNumber);
}
