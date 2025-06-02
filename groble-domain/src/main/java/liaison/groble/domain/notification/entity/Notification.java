package liaison.groble.domain.notification.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.domain.notification.enums.NotificationReadStatus;
import liaison.groble.domain.notification.enums.NotificationType;
import liaison.groble.domain.notification.enums.SubNotificationType;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "notifications",
    indexes = {@Index(name = "idx_receiver", columnList = "receiver_user_id")})
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false)
  private NotificationType notificationType;

  @Enumerated(EnumType.STRING)
  @Column(name = "sub_notification_type", nullable = false)
  private SubNotificationType subNotificationType; // 알림 보조 타입

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_read_status")
  private NotificationReadStatus notificationReadStatus; // 알림 상태

  // 모든 세부 정보를 JSON으로 저장하는 단일 컬럼
  @Column(name = "details", columnDefinition = "JSON")
  private String details;

  // 세부 정보 객체들
  @Transient private SystemDetails systemDetails;
  @Transient private ReviewDetails reviewDetails;
  @Transient private CertifyDetails certifyDetails;

  private LocalDateTime createdAt;

  // JSON 변환 로직을 처리하는 메서드들
  @PrePersist
  @PreUpdate
  public void beforeSave() {
    // 알림 타입에 따라 적절한 객체를 JSON으로 변환
    ObjectMapper mapper = new ObjectMapper();
    try {
      switch (notificationType) {
        case CERTIFY:
          details = certifyDetails != null ? mapper.writeValueAsString(certifyDetails) : null;
          break;
        case REVIEW:
          details = reviewDetails != null ? mapper.writeValueAsString(reviewDetails) : null;
          break;
        case SYSTEM:
          details = systemDetails != null ? mapper.writeValueAsString(systemDetails) : null;
          break;
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 변환 오류", e);
    }
  }

  @PostLoad
  public void afterLoad() {
    // JSON에서 적절한 객체로 역직렬화
    if (details == null) return;

    ObjectMapper mapper = new ObjectMapper();
    try {
      switch (notificationType) {
        case CERTIFY:
          certifyDetails = mapper.readValue(details, CertifyDetails.class);
          break;
        case REVIEW:
          reviewDetails = mapper.readValue(details, ReviewDetails.class);
          break;
        case SYSTEM:
          systemDetails = mapper.readValue(details, SystemDetails.class);
          break;
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 변환 오류", e);
    }
  }

  // 현재 알림 타입에 맞는 세부 정보 객체를 반환하는 편의 메서드
  public Object getDetails() {
    switch (notificationType) {
      case CERTIFY:
        return certifyDetails;
      case REVIEW:
        return reviewDetails;
      case SYSTEM:
        return systemDetails;
      default:
        return null;
    }
  }
}
