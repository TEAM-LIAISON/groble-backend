package liaison.groble.domain.notification.scheduled.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationRunStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "scheduled_notification_runs")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ScheduledNotificationRun extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scheduled_notification_id", nullable = false)
  private ScheduledNotification scheduledNotification;

  @Column(name = "execution_time", nullable = false)
  private LocalDateTime executionTime;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private ScheduledNotificationRunStatus status;

  @Column(name = "total_targets")
  private Integer totalTargets;

  @Column(name = "success_count")
  private Integer successCount;

  @Column(name = "fail_count")
  private Integer failCount;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "retry_count")
  private Integer retryCount;

  @Version private Long version;

  public void markRunning(LocalDateTime startedAt) {
    this.status = ScheduledNotificationRunStatus.RUNNING;
    this.startedAt = startedAt;
  }

  public void markCompleted(int successCount, int failCount, LocalDateTime completedAt) {
    this.status = ScheduledNotificationRunStatus.COMPLETED;
    this.successCount = successCount;
    this.failCount = failCount;
    this.completedAt = completedAt;
  }

  public void markFailed(String errorMessage, LocalDateTime completedAt) {
    this.status = ScheduledNotificationRunStatus.FAILED;
    this.errorMessage = errorMessage;
    this.completedAt = completedAt;
  }

  public void markCancelled(LocalDateTime cancelledAt) {
    this.status = ScheduledNotificationRunStatus.CANCELLED;
    this.completedAt = cancelledAt;
  }
}
