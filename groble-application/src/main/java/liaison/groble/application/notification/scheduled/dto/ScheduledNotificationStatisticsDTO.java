package liaison.groble.application.notification.scheduled.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduledNotificationStatisticsDTO {
  private final long totalNotifications;
  private final long readyCount;
  private final long sentCount;
  private final long failedCount;
  private final long cancelledCount;
}
