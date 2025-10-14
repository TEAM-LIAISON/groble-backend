package liaison.groble.application.notification.scheduled.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduledNotificationChannelStatisticsDTO {
  private final long scheduled;
  private final long sent;
  private final double deliveryRate;
}
