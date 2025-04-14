package liaison.groble.domain.port;

public interface VerificationCodePort {
  void saveVerificationCode(String email, String code, long expirationTimeInMinutes);

  String getVerificationCode(String email);

  boolean validateVerificationCode(String email, String code);

  void removeVerificationCode(String email);
}
