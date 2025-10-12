package liaison.groble.application.notification.scheduled.command;

import java.time.LocalDateTime;

import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSendType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateScheduledNotificationCommand {
  private final ScheduledNotificationChannel channel;
  private final ScheduledNotificationSendType sendType;
  private final String title;
  private final String content;
  private final String bizTemplateCode;
  private final String bizSenderKey;
  private final LocalDateTime scheduledAt;
  private final String repeatCron;
  private final ScheduledNotificationSegmentType segmentType;
  private final String segmentPayload;
  private final String timezone;
  private final Long adminId;
}
