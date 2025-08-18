package liaison.groble.domain.dashboard.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.common.enums.PeriodType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "market_view_stats",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_mvs_market_date_period",
            columnNames = {"market_id", "stat_date", "period_type"}))
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class MarketViewStats extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "market_id", nullable = false)
  private Long marketId;

  @Column(name = "stat_date", nullable = false)
  private LocalDate statDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false, length = 16)
  private PeriodType periodType;

  @Column(name = "view_count", nullable = false, columnDefinition = "bigint default 0")
  private Long viewCount = 0L;

  @Column(name = "unique_viewer_count", nullable = false, columnDefinition = "bigint default 0")
  private Long uniqueViewerCount = 0L;

  @Column(name = "logged_in_viewer_count", nullable = false, columnDefinition = "bigint default 0")
  private Long loggedInViewerCount = 0L;
}
