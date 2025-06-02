package liaison.groble.domain.port;

public interface EmailSenderPort {
  void sendVerificationEmail(String to, String verificationCode);

  // 발급된 토큰을 담아서 URL 형태로 이메일을 발송
  void sendPasswordResetEmail(String to, String resetUrl);
}
