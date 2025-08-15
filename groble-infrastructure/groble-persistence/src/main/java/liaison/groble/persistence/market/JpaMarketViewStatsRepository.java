package liaison.groble.persistence.market;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.market.entity.MarketViewStats;

public interface JpaMarketViewStatsRepository extends JpaRepository<MarketViewStats, Long> {
  void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType);
}
