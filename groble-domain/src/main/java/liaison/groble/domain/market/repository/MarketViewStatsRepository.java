package liaison.groble.domain.market.repository;

import java.time.LocalDate;
import java.util.List;

import liaison.groble.domain.market.entity.MarketViewStats;

public interface MarketViewStatsRepository {
  void saveAll(List<MarketViewStats> stats);

  void deleteByStatDateAndPeriodType(LocalDate date, MarketViewStats.PeriodType periodType);
}
