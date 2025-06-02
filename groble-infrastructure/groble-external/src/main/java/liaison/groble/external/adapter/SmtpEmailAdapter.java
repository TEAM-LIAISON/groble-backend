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
      message.addHeader("List-Unsubscribe", "<mailto:groble@groble.im>");
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      // ── 헤더 설정
      helper.setFrom(String.format("Groble <%s>", fromEmail));
      helper.setReplyTo("groble@groble.im");
      helper.setTo(to);
      helper.setSubject("Groble 서비스 이메일 인증 코드");

      // ── 플레인 텍스트
      String plainText =
          "Groble 이메일 인증 코드\n\n"
              + "아래 인증코드를 5분 내에 입력해주세요:\n\n"
              + verificationCode
              + "\n\n"
              + "문의: groble@groble.im\n"
              + "수신거부: groble@groble.im";

      // ── HTML 본문
      String htmlContent =
          "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff;\">\n"
              + "  <tr><td align=\"center\">\n"
              + "    <table width=\"100%\" style=\"max-width:500px;margin:0 auto;\">\n"
              + "      <tr><td style=\"padding:32px 20px;text-align:center;\">\n"
              + "        <a href=\""
              + frontendUrl
              + "\" style=\"text-decoration:none;\">\n"
              + "          <img src=\"https://image.groble.im/static/mail/Groble_Color.png\" alt=\"Groble\" width=\"170\"/>\n"
              + "        </a>\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"text-align:center;color:#171717;font-size:24px;font-weight:700;padding:0 20px 16px;\">\n"
              + "        이메일 인증코드\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"text-align:center;color:#171717;font-size:15px;padding:0 20px 32px;\">\n"
              + "        아래 인증코드를 입력해주세요.<br/>인증코드는 5분간 유효합니다.\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"text-align:center;padding:0 20px 32px;\">\n"
              + "        <div style=\"width:100%;text-align:center;font-size:32px;font-weight:700;letter-spacing:12px;background-color:#F7F7F8;border-radius:12px;padding:20px 0;color:#171717;\">\n"
              + verificationCode
              + "        </div>\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"text-align:center;font-size:12px;color:#C2C4C8;padding:0 20px 32px;\">\n"
              + "        인증코드를 입력하시면, 절차가 안전하게 진행됩니다.\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"padding:0 20px;\">\n"
              + "        <div style=\"border-top:1px solid #e5e7eb;padding-top:32px;color:#C2C4C8;font-size:11px;line-height:1.6;text-align:left;\">\n"
              + "          본 메일은 발신전용이며, 문의에 대한 회신은 처리되지 않습니다.<br/>\n"
              + "          Groble에 관하여 궁금하신 점이나 불편한 사항은 언제라도 <a href=\"mailto:groble@groble.im\">groble@groble.im</a>으로 연락해주세요.<br/>\n"
              + "          수신거부는 <a href=\"mailto:groble@groble.im\">여기</a>를 클릭해 주세요.\n"
              + "        </div>\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"height:40px;\"></td></tr>\n"
              + "    </table>\n"
              + "  </td></tr>\n"
              + "</table>";

      // ── 텍스트+HTML 이중본 전송
      helper.setText(plainText, htmlContent);

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
      message.addHeader("List-Unsubscribe", "<mailto:groble@groble.im>");
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(String.format("Groble <%s>", fromEmail));
      helper.setReplyTo("groble@groble.im");
      helper.setTo(to);
      helper.setSubject("Groble 비밀번호 재설정");

      String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

      // ── 플레인 텍스트
      String plainText =
          "Groble 비밀번호 재설정 안내\n\n"
              + "아래 링크를 클릭하여 비밀번호를 재설정하세요 (24시간 내 유효):\n"
              + resetUrl
              + "\n\n"
              + "문의: groble@groble.im\n"
              + "수신거부: groble@groble.im";

      // ── HTML 본문
      String htmlContent =
          "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;\">\n"
              + "  <tr><td align=\"center\">\n"
              + "    <table width=\"100%\" style=\"max-width:500px;margin:0 auto;\">\n"
              + "      <tr><td style=\"padding:32px 20px;text-align:left;\">\n"
              + "        <a href=\""
              + frontendUrl
              + "\" style=\"text-decoration:none;\">\n"
              + "          <img src=\"https://image.groble.im/static/mail/Groble_Color.png\" alt=\"Groble\" width=\"170\"/>\n"
              + "        </a>\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"padding:0 20px 16px;color:#111827;font-size:24px;font-weight:700;\">\n"
              + "        비밀번호 재설정\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"padding:0 20px 32px;color:#374151;font-size:15px;line-height:1.6;\">\n"
              + "        회원님의 Groble 계정(<b>"
              + to
              + "</b>)에 대한 비밀번호 재설정 요청을 접수했습니다.<br/>\n"
              + "        아래 링크는 24시간 이후 만료되며, 한 번만 사용할 수 있습니다.\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"text-align:center;padding:0 20px 32px;\">\n"
              + "        <a href=\""
              + resetUrl
              + "\" style=\"display:block;padding:16px 0;width:100%;background-color:#00FCB4;border-radius:8px;color:#000;font-size:16px;font-weight:600;text-decoration:none;text-align:center;\">\n"
              + "          비밀번호 재설정\n"
              + "        </a>\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"border-top:1px solid #e5e7eb;padding:32px 20px;color:#C2C4C8;font-size:11px;line-height:1.6;text-align:left;\">\n"
              + "        본 메일은 발신전용이며, 문의에 대한 회신은 처리되지 않습니다.<br/>\n"
              + "        Groble에 관련하여 궁금하신 점이나 불편한 사항은 언제라도 <a href=\"mailto:groble@groble.im\">groble@groble.im</a>으로 연락해주세요.<br/>\n"
              + "          수신거부는 <a href=\"mailto:groble@groble.im\">여기</a>를 클릭해 주세요.\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"height:40px;\"></td></tr>\n"
              + "    </table>\n"
              + "  </td></tr>\n"
              + "</table>";

      helper.setText(plainText, htmlContent);

      emailSender.send(message);
      log.info("비밀번호 재설정 이메일 발송 완료: {}", to);

    } catch (MessagingException e) {
      log.error("비밀번호 재설정 이메일 발송 실패: {}", e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    }
  }
}
