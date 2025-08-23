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

import org.hibernate.annotations.Comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "market_referrer_events",
    indexes = {
      @Index(name = "idx_mre_market_date", columnList = "market_id, event_date"),
      @Index(name = "idx_mre_referrer", columnList = "referrer_stats_id")
    })
@Comment("유입 경로 이벤트 (개별 방문 기록)")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class MarketReferrerEvent {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "referrer_stats_id", nullable = false)
  @Comment("MarketReferrerStats ID")
  private Long referrerStatsId;

  @Column(name = "market_id", nullable = false)
  @Comment("마켓 ID")
  private Long marketId;

  @Column(name = "event_date", nullable = false)
  @Comment("이벤트 발생 일시")
  private LocalDateTime eventDate;
}
