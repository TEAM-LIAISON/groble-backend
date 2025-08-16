package liaison.groble.domain.dashboard.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마켓 조회 로그를 관리하는 엔티티
 *
 * @author 권동민
 * @since 2025-07-27
 */
@Entity
@Table(
    name = "market_view_logs",
    indexes = {
      @Index(name = "idx_mvl_market_viewed", columnList = "market_id, viewed_at"),
      @Index(name = "idx_mvl_visitor_hash", columnList = "market_id, viewed_at, visitor_hash")
    })
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class MarketViewLog {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "market_id", nullable = false)
  private Long marketId;

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
