package liaison.groble.external.discord.service.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.content.ContentRegisterCreateReportDTO;
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
      ContentRegisterCreateReportDTO contentRegisterCreateReportDTO) {
    var msg =
        "## 콘텐츠 판매하기 알림"
            + "\n**회원 닉네임:** "
            + contentRegisterCreateReportDTO.nickname()
            + "\n**콘텐츠 ID:** "
            + contentRegisterCreateReportDTO.contentId()
            + "\n**콘텐츠 제목:** "
            + contentRegisterCreateReportDTO.contentTitle()
            + "\n**콘텐츠 유형 (자료/코칭):** "
            + contentRegisterCreateReportDTO.contentType()
            + "\n**콘텐츠 심사 요청 시각 (Asia/Seoul):** "
            + contentRegisterCreateReportDTO.createdAt()
            + "\n콘텐츠 판매하기가 시작되었습니다.\n";

    discordService.sendMessages(url, msg);
  }
}
