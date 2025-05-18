package liaison.groble.domain.port;

public interface VerificationCodePort {
  void saveVerificationCode(String email, String code, long expirationTimeInMinutes);

  String getVerificationCode(String email);

  boolean validateVerificationCode(String email, String code);

  void removeVerificationCode(String email);

  // 비밀번호 재설정 토큰, 이메일을 저장하는 메서드
  void savePasswordResetCode(String email, String token, long expirationTimeInMinutes);

  // 비밀번호 재설정 토큰을 검증하는 메서드
  boolean validatePasswordResetCode(String token);

  String getPasswordResetEmail(String token);

  // 비밀번호 재설정 토큰을 삭제하는 메서드
  void removePasswordResetCode(String token);

  void saveVerifiedFlag(String email, long expirationTimeInMinutes);

  boolean validateVerifiedFlag(String email);
}
