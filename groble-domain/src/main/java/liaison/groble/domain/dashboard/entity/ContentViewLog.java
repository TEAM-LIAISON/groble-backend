package liaison.groble.domain.dashboard.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 콘텐츠 조회 로그를 관리하는 엔티티 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "content_view_logs",
    indexes = {
      @Index(name = "idx_cvl_content_viewed", columnList = "content_id, viewed_at"),
      @Index(name = "idx_cvl_visitor_hash", columnList = "content_id, viewed_at, visitor_hash")
    })
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class ContentViewLog {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "content_id", nullable = false)
  private Long contentId;

  @Column(name = "viewer_id")
  private Long viewerId;

  @Column(name = "viewer_ip", length = 45)
  private String viewerIp;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "visitor_hash", length = 64)
  private String visitorHash; // SHA-256(salt|ip|userAgent)

  @Column(name = "referer", length = 500)
  private String referer;

  @CreatedDate
  @Column(name = "viewed_at", nullable = false, updatable = false)
  private LocalDateTime viewedAt;
}
