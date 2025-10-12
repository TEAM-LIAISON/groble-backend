package liaison.groble.application.notification.scheduled.dto;

import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduledNotificationSegmentDTO {
  private final Long id;
  private final String name;
  private final String description;
  private final ScheduledNotificationSegmentType segmentType;
  private final String segmentPayload;
  private final boolean active;
}
