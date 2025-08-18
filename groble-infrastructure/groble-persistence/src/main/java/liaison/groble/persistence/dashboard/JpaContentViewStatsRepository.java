package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.ContentViewStats;

public interface JpaContentViewStatsRepository extends JpaRepository<ContentViewStats, Long> {
  void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType);

  @Query(
      "SELECT SUM(cvs.viewCount) FROM ContentViewStats cvs "
          + "WHERE cvs.contentId IN :contentIds "
          + "AND cvs.periodType = 'DAILY' "
          + "AND cvs.statDate BETWEEN :startDate AND :endDate")
  Long getTotalContentViews(
      @Param("contentIds") List<Long> contentIds,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
