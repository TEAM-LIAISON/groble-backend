package liaison.groble.domain.content.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.market.entity.MarketViewStats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class ContentViewStats extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "content_id", nullable = false)
  private Long contentId;

  @Column(name = "stat_date", nullable = false)
  private LocalDate statDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false, length = 10)
  private MarketViewStats.PeriodType periodType; // DAILY, MONTHLY

  @Column(name = "view_count", nullable = false)
  private Long viewCount;

  @Column(name = "unique_viewer_count", nullable = false)
  private Long uniqueViewerCount;

  @Column(name = "logged_in_viewer_count", nullable = false)
  private Long loggedInViewerCount;

  public enum PeriodType {
    DAILY,
    MONTHLY
  }
}
