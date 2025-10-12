package liaison.groble.domain.notification.scheduled.dto;

import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;

public record ScheduledNotificationChannelAggregate(
    ScheduledNotificationChannel channel,
    long totalScheduled,
    long totalSent,
    long totalCancelled) {}
