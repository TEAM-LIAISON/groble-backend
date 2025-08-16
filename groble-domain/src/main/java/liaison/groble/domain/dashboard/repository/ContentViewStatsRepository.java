package liaison.groble.domain.dashboard.repository;

import java.time.LocalDate;
import java.util.List;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.ContentViewStats;

public interface ContentViewStatsRepository {
  void saveAll(List<ContentViewStats> stats);

  void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType);

  Long getTotalContentViews(List<Long> contentIds, LocalDate startDate, LocalDate endDate);
}
