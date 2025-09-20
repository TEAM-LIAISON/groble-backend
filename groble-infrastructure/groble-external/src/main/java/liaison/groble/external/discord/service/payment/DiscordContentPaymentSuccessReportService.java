package liaison.groble.external.discord.service.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.payment.ContentPaymentSuccessReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordContentPaymentSuccessReportService
    implements ContentPaymentSuccessReportService {
  @Value("${discord.webhook.alert.payment-success.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendContentPaymentSuccessReport(
      final ContentPaymentSuccessReportDTO contentPaymentSuccessReportDTO) {
    var dto = contentPaymentSuccessReportDTO;
    var msgBuilder = new StringBuilder("## 콘텐츠 결제 알림\n\n");

    if (dto.userId() != null) {
      msgBuilder
          .append("**회원 ID:** ")
          .append(dto.userId())
          .append("\n**회원 닉네임:** ")
          .append(nullSafe(dto.nickname()))
          .append('\n');
    } else {
      msgBuilder
          .append("**비회원 ID:** ")
          .append(nullSafe(dto.guestUserId()))
          .append("\n**비회원 이름:** ")
          .append(nullSafe(dto.guestUserName()))
          .append('\n');
    }

    msgBuilder
        .append("**구매한 콘텐츠 ID:** ")
        .append(dto.contentId())
        .append("\n**구매한 콘텐츠 제목:** ")
        .append(nullSafe(dto.contentTitle()))
        .append("\n**구매한 콘텐츠 유형:** ")
        .append(nullSafe(dto.contentType()))
        .append("\n**구매한 콘텐츠 옵션 ID:** ")
        .append(nullSafe(dto.optionId()))
        .append("\n**구매한 콘텐츠 옵션 이름:** ")
        .append(nullSafe(dto.selectedOptionName()))
        .append("\n**구매 주문 번호:** ")
        .append(nullSafe(dto.merchantUid()))
        .append("\n**구매 일시:** ")
        .append(nullSafe(dto.purchasedAt()))
        .append("\n\n콘텐츠 결제 알림입니다.\n");

    discordService.sendMessages(url, msgBuilder.toString());
  }

  private String nullSafe(Object value) {
    return value == null ? "미등록" : value.toString();
  }
}
