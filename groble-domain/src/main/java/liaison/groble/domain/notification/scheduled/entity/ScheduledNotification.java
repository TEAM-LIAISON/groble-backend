package liaison.groble.domain.notification.scheduled.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSendType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "scheduled_notifications",
    indexes = {
      @Index(name = "idx_scheduled_notifications_status", columnList = "status"),
      @Index(name = "idx_scheduled_notifications_scheduled_at", columnList = "scheduled_at"),
      @Index(name = "idx_scheduled_notifications_channel", columnList = "channel")
    })
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ScheduledNotification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(STRING)
  @Column(nullable = false, length = 30)
  private ScheduledNotificationChannel channel;

  @Enumerated(STRING)
  @Column(name = "send_type", nullable = false, length = 20)
  private ScheduledNotificationSendType sendType;

  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private ScheduledNotificationStatus status;

  @Column(length = 120)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(name = "biz_template_code", length = 50)
  private String bizTemplateCode;

  @Column(name = "biz_sender_key", length = 50)
  private String bizSenderKey;

  @Column(name = "scheduled_at", nullable = false)
  private LocalDateTime scheduledAt;

  @Column(name = "repeat_cron", length = 120)
  private String repeatCron;

  @Enumerated(STRING)
  @Column(name = "segment_type", nullable = false, length = 30)
  private ScheduledNotificationSegmentType segmentType;

  @Column(name = "segment_payload", columnDefinition = "JSON")
  private String segmentPayload;

  @Column(length = 40)
  private String timezone;

  @Column(name = "created_by_admin_id", nullable = false)
  private Long createdByAdminId;

  @Column(name = "updated_by_admin_id")
  private Long updatedByAdminId;

  @Version private Long version;

  @Builder.Default
  @OneToMany(mappedBy = "scheduledNotification", fetch = FetchType.LAZY)
  private List<ScheduledNotificationRun> runs = new ArrayList<>();

  public void update(
      ScheduledNotificationChannel channel,
      ScheduledNotificationSendType sendType,
      ScheduledNotificationStatus status,
      String title,
      String content,
      String bizTemplateCode,
      String bizSenderKey,
      LocalDateTime scheduledAt,
      String repeatCron,
      ScheduledNotificationSegmentType segmentType,
      String segmentPayload,
      String timezone,
      Long updatedByAdminId) {
    this.channel = channel;
    this.sendType = sendType;
    this.status = status;
    this.title = title;
    this.content = content;
    this.bizTemplateCode = bizTemplateCode;
    this.bizSenderKey = bizSenderKey;
    this.scheduledAt = scheduledAt;
    this.repeatCron = repeatCron;
    this.segmentType = segmentType;
    this.segmentPayload = segmentPayload;
    this.timezone = timezone;
    this.updatedByAdminId = updatedByAdminId;
  }

  public void markCancelled(Long adminId) {
    this.status = ScheduledNotificationStatus.CANCELLED;
    this.updatedByAdminId = adminId;
  }

  public boolean isRecurring() {
    return sendType == ScheduledNotificationSendType.RECURRING;
  }
}
