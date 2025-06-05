package liaison.groble.external.discord.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.MemberCreateReportDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordMemberReportService implements MemberReportService {

  @Value("${discord.webhook.alert.member.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendCreateMemberReport(final MemberCreateReportDto memberCreateReportDto) {
    var msg =
        "## 회원 생성 알림"
            + "\n\n**회원 생성 ID:** "
            + memberCreateReportDto.userId()
            + "\n**회원 생성 시간:** "
            + memberCreateReportDto.createdAt()
            + "\n**회원 닉네임:** "
            + memberCreateReportDto.nickname()
            + "님이 회원가입하셨습니다.";

    discordService.sendMessages(url, msg);
  }
}
