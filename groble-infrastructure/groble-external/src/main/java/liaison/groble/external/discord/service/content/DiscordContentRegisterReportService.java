package liaison.groble.external.discord.service.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.ContentRegisterCreateReportDto;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordContentRegisterReportService implements ContentRegisterReportService {

  @Value("${discord.webhook.alert.content-register.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendCreateContentRegisterReport(
      ContentRegisterCreateReportDto contentRegisterCreateReportDto) {
    var msg =
        "## 콘텐츠 심사 요청 알림"
            + "\n**회원 닉네임:** "
            + contentRegisterCreateReportDto.nickname()
            + "\n**콘텐츠 ID:** "
            + contentRegisterCreateReportDto.contentId()
            + "\n**콘텐츠 제목:** "
            + contentRegisterCreateReportDto.contentTitle()
            + "\n**콘텐츠 유형 (자료/코칭):** "
            + contentRegisterCreateReportDto.contentType()
            + "\n**콘텐츠 심사 요청 시각 (Asia/Seoul):** "
            + contentRegisterCreateReportDto.createdAt()
            + "\n님이 콘텐츠 심사 요청을 하셨습니다.";

    discordService.sendMessages(url, msg);
  }
}
