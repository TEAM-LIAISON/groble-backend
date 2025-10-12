package liaison.groble.application.notification.scheduled.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduledNotificationStatisticsDTO {
  private final long totalScheduled;
  private final long totalSent;
  private final long totalCancelled;
  private final double deliveryRate;
  private final ChannelStatisticsDTO channelStats;

  @Getter
  @Builder
  public static class ChannelStatisticsDTO {
    private final ScheduledNotificationChannelStatisticsDTO system;
    private final ScheduledNotificationChannelStatisticsDTO kakao;
  }
}
