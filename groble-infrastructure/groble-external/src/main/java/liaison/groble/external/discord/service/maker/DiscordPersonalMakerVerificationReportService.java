package liaison.groble.external.discord.service.maker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.PersonalMakerVerificationCreateReportDto;
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
      final PersonalMakerVerificationCreateReportDto personalMakerVerificationCreateReportDto) {
    var msg =
        "## 개인 메이커 인증 요청 알림"
            + "\n\n**회원 생성 ID:** "
            + personalMakerVerificationCreateReportDto.userId()
            + "\n**회원 닉네임:** "
            + personalMakerVerificationCreateReportDto.nickname()
            + "\n**기입한 예금주명:** "
            + personalMakerVerificationCreateReportDto.bankAccountOwner()
            + "\n**기입한 정산 받을 은행:** "
            + personalMakerVerificationCreateReportDto.bankName()
            + "\n**기입한 정산 받을 계좌:** "
            + personalMakerVerificationCreateReportDto.bankAccountNumber()
            + "\n**업로드한 통장 사본 첨부 URL:** "
            + personalMakerVerificationCreateReportDto.copyOfBankbookUrl()
            + "\n님이 개인 메이커 인증 요청을 하셨습니다.";

    discordService.sendMessages(url, msg);
  }
}
