package liaison.groble.domain.notification.scheduled.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "scheduled_notification_segments")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ScheduledNotificationSegment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 255)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "segment_type", nullable = false, length = 30)
  private ScheduledNotificationSegmentType segmentType;

  @Column(name = "segment_payload", columnDefinition = "JSON", nullable = false)
  private String segmentPayload;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "created_by_admin_id", nullable = false)
  private Long createdByAdminId;

  @Column(name = "updated_by_admin_id")
  private Long updatedByAdminId;

  @Version private Long version;

  public void update(
      String name,
      String description,
      String segmentPayload,
      boolean active,
      Long adminId,
      ScheduledNotificationSegmentType segmentType) {
    this.name = name;
    this.description = description;
    this.segmentPayload = segmentPayload;
    this.active = active;
    this.updatedByAdminId = adminId;
    this.segmentType = segmentType;
  }

  public void deactivate(Long adminId) {
    this.active = false;
    this.updatedByAdminId = adminId;
  }
}
