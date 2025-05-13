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
  public void sendPasswordResetEmail(String to, String resetToken) {
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Groble 비밀번호 재설정");

      String htmlContent =
          "<div style='max-width:600px; margin:0 auto; font-family:Arial, sans-serif;'>"
              + "  <div style='text-align:left; padding:20px 0;'>"
              + "    <img src='"
              + frontendUrl
              + "/assets/groble-logo.png' alt='Groble' style='height:40px;'/>"
              + "  </div>"
              + "  <h1 style='font-size:24px; font-weight:bold; margin-top:40px; margin-bottom:20px;'>비밀번호 재설정</h1>"
              + "  <p style='font-size:16px; line-height:1.5; margin-bottom:30px;'>"
              + "    회원님의 Groble 계정("
              + to
              + ")에 대한<br>"
              + "    비밀번호 재설정 요청을 접수했습니다. 아래 링크는<br>"
              + "    24시간 이후 만료되며, 한 번만 사용할 수 있습니다."
              + "  </p>"
              + "  <div style='text-align:center; margin:40px 0;'>"
              + "    <a href='"
              + resetUrl
              + "' style='display:inline-block; background-color:#00FCB4; color:#000000; text-decoration:none; font-weight:bold; padding:15px 0; width:100%; border-radius:8px; font-size:18px;'>비밀번호 재설정</a>"
              + "  </div>"
              + "  <p style='font-size:14px; color:#777777; line-height:1.5; margin-top:40px;'>"
              + "    본 메일은 발신전용이며, 문의에 대한 회신은 처리되지 않습니다.<br>"
              + "    Groble에 관련하여 궁금하신 점이나 불편한 사항은 언제라도<br>"
              + "    @@으로 연락해주세요."
              + "  </p>"
              + "</div>";

      helper.setText(htmlContent, true);

      emailSender.send(message);
      log.info("비밀번호 재설정 이메일 발송 완료: {}", to);
    } catch (MessagingException e) {
      log.error("비밀번호 재설정 이메일 발송 실패: {}", e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    }
  }
}
