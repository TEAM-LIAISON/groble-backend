package liaison.groble.external.discord.service.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.payment.ContentPaymentRefundReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordContentPaymentRefundRequestService
    implements ContentPaymentRefundRequestService {

  @Value("${discord.webhook.alert.payment-refund-request.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendContentPaymentRefundRequestReport(
      final ContentPaymentRefundReportDTO contentPaymentRefundReportDTO) {
    var msg =
        "## 콘텐츠 결제 취소 요청 알림"
            + "\n\n**회원 ID:** "
            + contentPaymentRefundReportDTO.userId()
            + "\n**회원 닉네임:** "
            + contentPaymentRefundReportDTO.nickname()
            + "\n**구매한 콘텐츠 ID:** "
            + contentPaymentRefundReportDTO.contentId()
            + "\n**구매한 콘텐츠 제목:** "
            + contentPaymentRefundReportDTO.contentTitle()
            + "\n**구매한 콘텐츠 유형:** "
            + contentPaymentRefundReportDTO.contentType()
            + "\n**구매한 콘텐츠 옵션 ID:** "
            + contentPaymentRefundReportDTO.optionId()
            + "\n**구매한 콘텐츠 옵션 이름:** "
            + contentPaymentRefundReportDTO.selectedOptionName()
            + "\n**구매 주문 번호:** "
            + contentPaymentRefundReportDTO.merchantUid()
            + "\n**구매 일시:** "
            + contentPaymentRefundReportDTO.purchasedAt()
            + "\n**취소 사유:** "
            + contentPaymentRefundReportDTO.cancelReason()
            + "\n**취소 요청 일시:** "
            + contentPaymentRefundReportDTO.cancelRequestedAt()
            + "\n\n콘텐츠 결제 취소 요청 알림입니다.\n";

    discordService.sendMessages(url, msg);
  }
}
