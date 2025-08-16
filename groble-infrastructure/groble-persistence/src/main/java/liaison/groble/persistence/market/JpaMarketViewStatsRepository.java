package liaison.groble.persistence.market;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.MarketViewStats;

public interface JpaMarketViewStatsRepository extends JpaRepository<MarketViewStats, Long> {
  void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType);
}
