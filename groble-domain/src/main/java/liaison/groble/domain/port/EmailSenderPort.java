package liaison.groble.domain.port;

public interface EmailSenderPort {
  void sendVerificationEmail(String to, String verificationCode);

  void sendPasswordResetEmail(String to, String resetToken, String resetUrl);
}
