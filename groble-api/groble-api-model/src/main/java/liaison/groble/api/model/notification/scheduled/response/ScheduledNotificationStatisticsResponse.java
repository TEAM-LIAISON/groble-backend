package liaison.groble.api.model.notification.scheduled.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "예약 알림 발송 통계 응답")
public class ScheduledNotificationStatisticsResponse {

  @Schema(description = "지정 기간 내 예약된 알림 수", example = "150")
  private long totalScheduled;

  @Schema(description = "성공적으로 발송된 알림 수", example = "140")
  private long totalSent;

  @Schema(description = "취소된 알림 수", example = "10")
  private long totalCancelled;

  @Schema(description = "전체 발송 성공률", example = "93.3")
  private double deliveryRate;

  @Schema(description = "채널별 통계")
  private ChannelStatsResponse channelStats;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChannelStatsResponse {

    @Schema(description = "시스템 알림 통계")
    private ChannelStatResponse system;

    @Schema(description = "카카오 알림톡 통계")
    private ChannelStatResponse kakao;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChannelStatResponse {

    @Schema(description = "해당 채널의 예약된 알림 수", example = "100")
    private long scheduled;

    @Schema(description = "해당 채널의 발송된 알림 수", example = "95")
    private long sent;

    @Schema(description = "해당 채널의 발송 성공률", example = "95.0")
    private double deliveryRate;
  }
}
