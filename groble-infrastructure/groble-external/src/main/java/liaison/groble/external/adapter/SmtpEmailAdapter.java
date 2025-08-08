package liaison.groble.external.adapter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
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

  // SOCKS 프록시 관련 필드 모두 제거

  @Override
  @Async("mailExecutor")
  @Retryable(
      value = {MailException.class, MessagingException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2))
  public void sendVerificationEmail(String to, String verificationCode) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("이메일 발송 시작 - 수신자: {}", to); // 프록시 로그 제거

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

      long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("인증 이메일 발송 완료 - 수신자: {}, 소요시간: {}ms", to, elapsedTime);

    } catch (MessagingException e) {
      log.error("인증 이메일 발송 실패 - 수신자: {}, 에러: {}", to, e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    } catch (MailException e) {
      log.error("메일 서버 연결 실패 - 수신자: {}, 에러: {}", to, e.getMessage(), e);
      throw new RuntimeException("메일 서버 연결에 실패했습니다. SOCKS 프록시 설정을 확인해주세요.", e);
    }
  }

  @Override
  @Async("mailExecutor")
  @Retryable(
      value = {MailException.class, MessagingException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2))
  public void sendPasswordResetEmail(String to, String resetToken) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("비밀번호 재설정 이메일 발송 시작 - 수신자: {}", to);

      MimeMessage message = createMimeMessage();
      message.addHeader("List-Unsubscribe", "<mailto:groble@groble.im>");
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(String.format("Groble <%s>", fromEmail));
      helper.setReplyTo("groble@groble.im");
      helper.setTo(to);
      helper.setSubject("Groble 비밀번호 재설정");

      String resetUrl = frontendUrl + "/auth/reset-password/new?token=" + resetToken;

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

      long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("비밀번호 재설정 이메일 발송 완료 - 수신자: {}, 소요시간: {}ms", to, elapsedTime);

    } catch (MessagingException e) {
      log.error("비밀번호 재설정 이메일 발송 실패 - 수신자: {}, 에러: {}", to, e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    } catch (MailException e) {
      log.error("메일 서버 연결 실패 - 수신자: {}, 에러: {}", to, e.getMessage(), e);
      throw new RuntimeException("메일 서버 연결에 실패했습니다. SOCKS 프록시 설정을 확인해주세요.", e);
    }
  }

  @Override
  @Async("mailExecutor")
  @Retryable(
      value = {MailException.class, MessagingException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2))
  public void sendSaleNotificationEmail(
      String to, String productName, BigDecimal price, LocalDateTime saleDate, Long contentId) {

    long startTime = System.currentTimeMillis();

    // 1) 포맷터 생성: "yyyy.MM.dd HH:mm"
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    // 2) LocalDateTime → 포맷된 문자열
    String formattedDate = saleDate.format(formatter);

    // 가격 포맷팅
    NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.KOREA);
    currencyFormatter.setGroupingUsed(true); // 천 단위 콤마 사용
    currencyFormatter.setMaximumFractionDigits(0); // 소수점 이하 제거
    String formattedPrice = currencyFormatter.format(price) + "원";

    try {
      log.info("판매 알림 이메일 발송 시작 - 수신자: {}, 상품: {}", to, productName);

      MimeMessage message = createMimeMessage();
      message.addHeader("List-Unsubscribe", "<mailto:groble@groble.im>");
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(String.format("Groble <%s>", fromEmail));
      helper.setReplyTo("groble@groble.im");
      helper.setTo(to);
      helper.setSubject("Groble 콘텐츠 판매 알림");

      String detailUrl = frontendUrl + "/manage/store/products/" + contentId;

      // ── 플레인 텍스트
      String plainText =
          "Groble 판매 알림\n\n"
              + "아래 링크를 클릭하여 판매 내역을 확인하세요.\n"
              + detailUrl
              + "\n\n"
              + "문의: groble@groble.im\n"
              + "수신거부: groble@groble.im";

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
              + "        상품 판매 알림\n"
              + "      </td></tr>\n"
              + "      <tr>\n"
              + "        <td style=\"padding:0 24px 32px; margin:0; color:#222; font-size:16px; font-weight:500;\">\n"
              + "          <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:transparent;\">\n"
              + "            <tr><td style=\"padding-bottom:8px;\">· 상품명: "
              + productName
              + "</td></tr>\n"
              + "            <tr><td style=\"padding-bottom:8px;\">· 판매 금액: "
              + formattedPrice
              + "</td></tr>\n"
              + "            <tr><td>· 판매 일시: "
              + formattedDate
              + "</td></tr>\n"
              + "          </table>\n"
              + "        </td>\n"
              + "      </tr>\n"
              + "      <tr><td style=\"text-align:center;padding:0 20px 32px;\">\n"
              + "        <a href=\""
              + detailUrl
              + "\" style=\"display:block;padding:16px 0;width:100%;background-color:#00FCB4;border-radius:8px;color:#000;font-size:16px;font-weight:600;text-decoration:none;text-align:center;\">\n"
              + "          확인하러 가기\n"
              + "        </a>\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"border-top:1px solid #e5e7eb;padding:32px 20px;color:#C2C4C8;font-size:11px;line-height:1.6;text-align:left;\">\n"
              + "        본 메일은 발신전용이며, 문의에 대한 회신은 처리되지 않습니다.<br/>\n"
              + "        Groble에 관련하여 궁금하신 점이나 불편한 사항은 언제라도 <a href=\"mailto:groble@groble.im\">groble@groble.im</a>으로 연락해주세요.<br/>\n"
              + "        수신거부는 <a href=\"mailto:groble@groble.im\">여기</a>를 클릭해 주세요.\n"
              + "      </td></tr>\n"
              + "      <tr><td style=\"height:40px;\"></td></tr>\n"
              + "    </table>\n"
              + "  </td></tr>\n"
              + "</table>";

      helper.setText(plainText, htmlContent);

      emailSender.send(message);

      long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("판매 알림 이메일 발송 완료 - 수신자: {}, 소요시간: {}ms", to, elapsedTime);

    } catch (MessagingException e) {
      log.error("판매 알림 이메일 발송 실패 - 수신자: {}, 에러: {}", to, e.getMessage(), e);
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
    } catch (MailException e) {
      log.error("메일 서버 연결 실패 - 수신자: {}, 에러: {}", to, e.getMessage(), e);
      throw new RuntimeException("메일 서버 연결에 실패했습니다. SOCKS 프록시 설정을 확인해주세요.", e);
    }
  }

  /** MimeMessage 생성 SOCKS 프록시는 시스템 프로퍼티로 이미 설정되어 있어야 함 */
  private MimeMessage createMimeMessage() {
    return emailSender.createMimeMessage();
  }
}
