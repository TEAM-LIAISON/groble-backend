package liaison.groble.external.discord.service.payment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.payment.PaymentFailureReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordPaymentFailureReportService implements PaymentFailureReportService {

  @Value("${discord.webhook.alert.payment-failure.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendPaymentFailureReport(PaymentFailureReportDTO dto) {
    StringBuilder message =
        new StringBuilder("## 결제 실패 알림\n\n")
            .append("**구매자명:** ")
            .append(nullSafe(dto.buyerName()))
            .append('\n')
            .append("**상품명:** ")
            .append(nullSafe(dto.productName()))
            .append('\n')
            .append("**상품옵션명:** ")
            .append(nullSafe(dto.productOptionName()))
            .append('\n')
            .append("**가격:** ")
            .append(formatAmount(dto.price()))
            .append('\n')
            .append("**실패 사유:** ")
            .append(nullSafe(dto.failureReason()))
            .append("\n\n결제 실패 건을 확인해주세요.\n");

    discordService.sendMessages(url, message.toString());
  }

  private String nullSafe(String value) {
    return value == null || value.isBlank() ? "미등록" : value;
  }

  private String formatAmount(BigDecimal price) {
    if (price == null) {
      return "미등록";
    }
    NumberFormat formatter = NumberFormat.getInstance(Locale.KOREA);
    formatter.setMinimumFractionDigits(0);
    formatter.setMaximumFractionDigits(Math.max(0, price.scale()));
    return formatter.format(price);
  }
}
