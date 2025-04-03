package liaison.groble.application.email.service;

public interface EmailService {

  void sendVerificationEmail(String to, String token);
}
