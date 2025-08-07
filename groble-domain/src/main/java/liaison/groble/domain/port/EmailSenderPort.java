package liaison.groble.domain.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface EmailSenderPort {
  // 인증 코드 발송
  void sendVerificationEmail(String to, String verificationCode);

  // 발급된 토큰을 담아서 URL 형태로 이메일을 발송
  void sendPasswordResetEmail(String to, String resetUrl);

  // 판매 안내 메일 발송
  void sendSaleNotificationEmail(
      String to, String productName, BigDecimal price, LocalDateTime saleDate, Long contentId);
}
