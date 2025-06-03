package liaison.groble.external.discord.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.BusinessMakerVerificationCreateReportDto;

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
      BusinessMakerVerificationCreateReportDto businessMakerVerificationCreateReportDto) {
    var msg =
        "## 회원 생성 알림"
            + "\n\n**회원 생성 ID:** "
            + businessMakerVerificationCreateReportDto.userId()
            + "\n**회원 닉네임:** "
            + businessMakerVerificationCreateReportDto.nickname()
            + "\n**기입한 예금주명:** "
            + businessMakerVerificationCreateReportDto.bankAccountOwner()
            + "\n**기입한 정산 받을 은행:** "
            + businessMakerVerificationCreateReportDto.bankName()
            + "\n**기입한 정산 받을 계좌:** "
            + businessMakerVerificationCreateReportDto.bankAccountNumber()
            + "\n**업로드한 통장 사본 첨부 URL:** "
            + businessMakerVerificationCreateReportDto.copyOfBankbookUrl()
            + "\n**선택한 사업자 유형:** "
            + businessMakerVerificationCreateReportDto.businessType()
            + "\n**기입한 업종:** "
            + businessMakerVerificationCreateReportDto.businessCategory()
            + "\n**기입한 업태:** "
            + businessMakerVerificationCreateReportDto.businessSector()
            + "\n**기입한 상호:** "
            + businessMakerVerificationCreateReportDto.businessName()
            + "\n**기입한 대표자 명:** "
            + businessMakerVerificationCreateReportDto.representativeName()
            + "\n**기입한 사업장 소재지:** "
            + businessMakerVerificationCreateReportDto.businessAddress()
            + "\n**업로드한 사업자 등록증 사본 첨부 URL:** "
            + businessMakerVerificationCreateReportDto.businessLicenseFileUrl()
            + "\n**기입한 세금계산서 수취 이메일:** "
            + businessMakerVerificationCreateReportDto.taxInvoiceEmail()
            + "\n님이 개인 및 법인 사업자 메이커 인증 요청을 하셨습니다.";

    discordService.sendMessages(url, msg);
  }
}
