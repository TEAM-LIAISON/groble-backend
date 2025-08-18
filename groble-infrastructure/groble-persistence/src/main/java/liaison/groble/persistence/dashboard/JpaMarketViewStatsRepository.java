package liaison.groble.persistence.dashboard;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.MarketViewStats;

public interface JpaMarketViewStatsRepository extends JpaRepository<MarketViewStats, Long> {
  void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType);

  @Query(
      "SELECT SUM(mvs.viewCount) FROM MarketViewStats mvs "
          + "JOIN Market m ON m.id = mvs.marketId "
          + "WHERE m.user.id = :sellerId "
          + "AND mvs.periodType = 'DAILY' "
          + "AND mvs.statDate BETWEEN :startDate AND :endDate")
  Long getTotalMarketViews(
      @Param("sellerId") Long sellerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
