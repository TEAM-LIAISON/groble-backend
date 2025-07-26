package liaison.groble.external.discord.service.maker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.PersonalMakerVerificationCreateReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordPersonalMakerVerificationReportService
    implements PersonalMakerVerificationReportService {

  @Value("${discord.webhook.alert.personal-maker.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendCreatePersonalMakerVerificationReport(
      final PersonalMakerVerificationCreateReportDTO personalMakerVerificationCreateReportDTO) {
    var msg =
        "## 개인 메이커 인증 요청 알림"
            + "\n\n**회원 ID:** "
            + personalMakerVerificationCreateReportDTO.userId()
            + "\n**회원 닉네임:** "
            + personalMakerVerificationCreateReportDTO.nickname()
            + "\n**기입한 예금주명:** "
            + personalMakerVerificationCreateReportDTO.bankAccountOwner()
            + "\n**기입한 정산 받을 은행:** "
            + personalMakerVerificationCreateReportDTO.bankName()
            + "\n**기입한 정산 받을 계좌:** "
            + personalMakerVerificationCreateReportDTO.bankAccountNumber()
            + "\n**업로드한 통장 사본 첨부 URL:** "
            + personalMakerVerificationCreateReportDTO.copyOfBankbookUrl()
            + "\n개인 메이커 인증 요청이 들어왔습니다.\n";

    discordService.sendMessages(url, msg);
  }
}
