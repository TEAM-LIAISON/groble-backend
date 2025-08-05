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
  @Value("${discord.webhook.alert.payment-refund-request.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendContentPaymentSuccessReport(
      final ContentPaymentSuccessReportDTO contentPaymentSuccessReportDTO) {
    var msg =
        "## 콘텐츠 결제 알림"
            + "\n\n**회원 ID:** "
            + contentPaymentSuccessReportDTO.userId()
            + "\n**회원 닉네임:** "
            + contentPaymentSuccessReportDTO.nickname()
            + "\n**구매한 콘텐츠 ID:** "
            + contentPaymentSuccessReportDTO.contentId()
            + "\n**구매한 콘텐츠 제목:** "
            + contentPaymentSuccessReportDTO.contentTitle()
            + "\n**구매한 콘텐츠 유형:** "
            + contentPaymentSuccessReportDTO.contentType()
            + "\n**구매한 콘텐츠 옵션 ID:** "
            + contentPaymentSuccessReportDTO.optionId()
            + "\n**구매한 콘텐츠 옵션 이름:** "
            + contentPaymentSuccessReportDTO.selectedOptionName()
            + "\n**구매 주문 번호:** "
            + contentPaymentSuccessReportDTO.merchantUid()
            + "\n**구매 일시:** "
            + contentPaymentSuccessReportDTO.purchasedAt()
            + "\n\n콘텐츠 결제 알림입니다.\n";

    discordService.sendMessages(url, msg);
  }
}
