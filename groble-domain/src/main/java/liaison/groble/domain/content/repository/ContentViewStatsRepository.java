package liaison.groble.domain.content.repository;

import java.time.LocalDate;
import java.util.List;

import liaison.groble.domain.content.entity.ContentViewStats;

public interface ContentViewStatsRepository {
  void saveAll(List<ContentViewStats> stats);

  void deleteByStatDateAndPeriodType(LocalDate date, ContentViewStats.PeriodType periodType);
}
