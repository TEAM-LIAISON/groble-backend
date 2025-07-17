package liaison.groble.external.discord.service.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.discord.dto.DeleteReviewRequestReportDTO;
import liaison.groble.external.discord.service.DiscordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscordDeleteReviewRequestReportService implements DeleteReviewRequestReportService {

  @Value("${discord.webhook.alert.delete-review-request.url}")
  private String url;

  private final DiscordService discordService;

  @Override
  public void sendDeleteReviewRequestReport(
      DeleteReviewRequestReportDTO deleteReviewRequestReportDTO) {
    var msg =
        "## 리뷰 삭제 요청 알림"
            + "\n**회원 ID:** "
            + deleteReviewRequestReportDTO.userId()
            + "\n**리뷰 ID:** "
            + deleteReviewRequestReportDTO.reviewId()
            + "\n에 대한 리뷰 삭제 요청이 접수되었습니다.";

    discordService.sendMessages(url, msg);
  }
}
