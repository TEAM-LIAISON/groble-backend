package liaison.groble.domain.notification.scheduled.dto;

public record ScheduledNotificationStatisticsAggregate(
    long totalScheduled, long totalSent, long totalCancelled) {}
