package liaison.groble.domain.dashboard.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.dto.FlatContentTotalViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatContentViewStatsDTO;

public interface ContentViewStatsCustomRepository {
  Page<FlatContentViewStatsDTO> findByContentIdAndPeriodTypeAndStatDateBetween(
      Long contentId,
      PeriodType periodType,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable);

  Page<FlatContentTotalViewStatsDTO> findTotalViewsByPeriodTypeAndStatDateBetween(
      PeriodType periodType, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
