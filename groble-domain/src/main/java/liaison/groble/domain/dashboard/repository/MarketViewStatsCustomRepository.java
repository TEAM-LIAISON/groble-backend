package liaison.groble.domain.dashboard.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.dto.FlatMarketViewStatsDTO;

public interface MarketViewStatsCustomRepository {
  Page<FlatMarketViewStatsDTO> findByMarketIdAndPeriodTypeAndStatDateBetween(
      Long marketId,
      PeriodType periodType,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable);
}
