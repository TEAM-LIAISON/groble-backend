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
          "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff;\">\n"
              + "    <tr>\n"
              + "      <td align=\"center\">\n"
              + "        <table width=\"100%\" style=\"max-width: 500px; margin: 0 auto;\">\n"
              + "          <tr>\n"
              + "            <td style=\"padding: 32px 20px 32px 20px; text-align: left;\">\n"
              + "              <img src=\"https://image.groble.im/static/mail/Groble_Color.png\" alt=\"Groble\" width=\"92\" />\n"
              + "            </td>\n"
              + "          </tr>\n"
              + "          <tr>\n"
              + "            <td style=\"padding: 0px 20px 16px 20px; text-align: left; color: #111827; font-size: 24px; font-weight: 700;\">\n"
              + "              비밀번호 재설정\n"
              + "            </td>\n"
              + "          </tr>\n"
              + "          <tr>\n"
              + "            <td style=\"padding: 0px 20px 32px 20px; color: #374151; font-size: 15px; line-height: 1.6;\">\n"
              + "              회원님의 Groble 계정("
              + to
              + ")에 대한 비밀번호 재설정 요청을 접수했습니다.<br />\n"
              + "              아래 링크는 24시간 이후 만료되며, 한 번만 사용할 수 있습니다.\n"
              + "            </td>\n"
              + "          </tr>\n"
              + "          <tr>\n"
              + "            <td style=\"text-align: center; padding: 0px 20px 32px 20px;\">\n"
              + "    <a href='"
              + resetUrl
              + "' style=\"display: inline-block; padding: 16px 0; width: 100%; background-color: #00FCB4; border-radius: 8px; color: #000000; font-size: 16px; font-weight: 600; text-decoration: none;\">비밀번호 재설정</a>\n"
              + "            </td>\n"
              + "          </tr>\n"
              + "          <tr>\n"
              + "            <td style=\"border-top: 1px solid #e5e7eb; padding: 32px 20px 0px 20px; color: #6b7280; font-size: 12px; line-height: 1.6;\">\n"
              + "              본 메일은 발신전용이며, 문의에 대한 회신은 처리되지 않습니다.<br />\n"
              + "              Groble에 관해 궁금하신 점이나 불편한 사항은 언제라도 groble@groble.im으로 연락해주세요.\n"
              + "            </td>\n"
              + "          </tr>\n"
              + "          <tr>\n"
              + "            <td style=\"height: 40px;\"></td>\n"
              + "          </tr>\n"
              + "        </table>\n"
              + "      </td>\n"
              + "    </tr>\n"
              + "  </table>";

      helper.setText(htmlContent, true);

      emailSender.send(message);
      log.info("비밀번호 재설정 이메일 발송 완료: {}", to);
    } catch (MessagingException e) {
      log.error("비밀번호 재설정 이메일 발송 실패: {}", e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    }
  }
}
