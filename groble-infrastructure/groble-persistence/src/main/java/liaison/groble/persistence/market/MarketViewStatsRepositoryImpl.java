package liaison.groble.persistence.market;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.MarketViewStats;
import liaison.groble.domain.dashboard.repository.MarketViewStatsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class MarketViewStatsRepositoryImpl implements MarketViewStatsRepository {
  private final JpaMarketViewStatsRepository jpaMarketViewStatsRepository;

  @Override
  public void saveAll(List<MarketViewStats> stats) {
    jpaMarketViewStatsRepository.saveAll(stats);
  }

  @Override
  public void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType) {
    jpaMarketViewStatsRepository.deleteByStatDateAndPeriodType(date, periodType);
  }
}
