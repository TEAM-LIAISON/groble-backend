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
      "SELECT COALESCE(SUM(cvs.viewCount), 0) FROM ContentViewStats cvs "
          + "WHERE cvs.contentId IN :contentIds "
          + "AND cvs.periodType = 'DAILY' "
          + "AND cvs.statDate BETWEEN :startDate AND :endDate")
  Long getTotalContentViewsQuery(
      @Param("contentIds") List<Long> contentIds,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  // 빈 리스트 체크를 포함한 default 메서드
  default Long getTotalContentViews(List<Long> contentIds, LocalDate startDate, LocalDate endDate) {
    if (contentIds == null || contentIds.isEmpty()) {
      return 0L;
    }
    return getTotalContentViewsQuery(contentIds, startDate, endDate);
  }
}
