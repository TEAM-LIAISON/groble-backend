package liaison.groble.external.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import liaison.groble.domain.port.EmailSenderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailAdapter implements EmailSenderPort {

  private final JavaMailSender emailSender;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  public void sendVerificationEmail(String to, String verificationCode) {
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Groble 서비스 이메일 인증 코드");

      String htmlContent =
          "<div style='margin:20px;'>"
              + "<h2>Groble 서비스 이메일 인증</h2>"
              + "<p>안녕하세요, Groble 서비스에 가입해주셔서 감사합니다.</p>"
              + "<p>아래 인증 코드를 입력하여 이메일 인증을 완료해주세요.</p>"
              + "<div style='background-color:#f8f9fa;padding:10px;font-size:24px;font-weight:bold;text-align:center;'>"
              + verificationCode
              + "</div>"
              + "<p>인증 코드는 15분 동안 유효합니다.</p>"
              + "<p>감사합니다.</p>"
              + "</div>";

      helper.setText(htmlContent, true);

      emailSender.send(message);
      log.info("인증 이메일 발송 완료: {}", to);
    } catch (MessagingException e) {
      log.error("인증 이메일 발송 실패: {}", e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public void sendPasswordResetEmail(String to, String resetToken, String resetUrl) {
    // 비밀번호 재설정 이메일 구현
    // ...
  }
}
