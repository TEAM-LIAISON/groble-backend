package liaison.groble.application.notification.scheduled.dto;

import java.time.LocalDateTime;

import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationRunStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduledNotificationRunDTO {
  private final Long id;
  private final Long scheduledNotificationId;
  private final LocalDateTime executionTime;
  private final LocalDateTime startedAt;
  private final LocalDateTime completedAt;
  private final ScheduledNotificationRunStatus status;
  private final Integer totalTargets;
  private final Integer successCount;
  private final Integer failCount;
  private final String errorMessage;
  private final Integer retryCount;
}
