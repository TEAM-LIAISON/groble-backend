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

import liaison.groble.domain.notification.entity.detail.CertifyDetails;
import liaison.groble.domain.notification.entity.detail.PurchaseDetails;
import liaison.groble.domain.notification.entity.detail.ReviewDetails;
import liaison.groble.domain.notification.entity.detail.SellDetails;
import liaison.groble.domain.notification.entity.detail.SystemDetails;
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

  // 세부 정보 객체들 / DB에 저장하지 않고 메모리에서만 사용
  @Transient private SystemDetails systemDetails;
  @Transient private ReviewDetails reviewDetails;
  @Transient private CertifyDetails certifyDetails;
  @Transient private PurchaseDetails purchaseDetails;
  @Transient private SellDetails sellDetails;

  private LocalDateTime createdAt;

  // JSON 변환 로직을 처리하는 메서드들
  @PrePersist
  @PreUpdate
  public void beforeSave() {
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
}
