package liaison.groble.external.discord.service.maker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.BusinessMakerVerificationCreateReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordBusinessMakerVerificationReportService
    implements BusinessMakerVerificationReportService {

  @Value("${discord.webhook.alert.business-maker.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendCreateBusinessMakerVerificationReport(
      BusinessMakerVerificationCreateReportDTO businessMakerVerificationCreateReportDTO) {
    var msg =
        "## 개인 및 법인 사업자 메이커 인증 요청 알림"
            + "\n\n**회원 생성 ID:** "
            + businessMakerVerificationCreateReportDTO.userId()
            + "\n**회원 닉네임:** "
            + businessMakerVerificationCreateReportDTO.nickname()
            + "\n**기입한 예금주명:** "
            + businessMakerVerificationCreateReportDTO.bankAccountOwner()
            + "\n**기입한 정산 받을 은행:** "
            + businessMakerVerificationCreateReportDTO.bankName()
            + "\n**기입한 정산 받을 계좌:** "
            + businessMakerVerificationCreateReportDTO.bankAccountNumber()
            + "\n**업로드한 통장 사본 첨부 URL:** "
            + businessMakerVerificationCreateReportDTO.copyOfBankbookUrl()
            + "\n**선택한 사업자 유형:** "
            + businessMakerVerificationCreateReportDTO.businessType()
            + "\n**기입한 업종:** "
            + businessMakerVerificationCreateReportDTO.businessCategory()
            + "\n**기입한 업태:** "
            + businessMakerVerificationCreateReportDTO.businessSector()
            + "\n**기입한 상호:** "
            + businessMakerVerificationCreateReportDTO.businessName()
            + "\n**기입한 대표자 명:** "
            + businessMakerVerificationCreateReportDTO.representativeName()
            + "\n**기입한 사업장 소재지:** "
            + businessMakerVerificationCreateReportDTO.businessAddress()
            + "\n**업로드한 사업자 등록증 사본 첨부 URL:** "
            + businessMakerVerificationCreateReportDTO.businessLicenseFileUrl()
            + "\n**기입한 세금계산서 수취 이메일:** "
            + businessMakerVerificationCreateReportDTO.taxInvoiceEmail()
            + "\n님이 개인 및 법인 사업자 메이커 인증 요청을 하셨습니다.";

    discordService.sendMessages(url, msg);
  }
}
