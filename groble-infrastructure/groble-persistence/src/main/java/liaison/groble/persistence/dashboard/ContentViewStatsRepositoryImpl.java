package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.ContentViewStats;
import liaison.groble.domain.dashboard.repository.ContentViewStatsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContentViewStatsRepositoryImpl implements ContentViewStatsRepository {
  private final JpaContentViewStatsRepository jpaContentViewStatsRepository;

  @Override
  public void saveAll(List<ContentViewStats> stats) {
    jpaContentViewStatsRepository.saveAll(stats);
  }

  @Override
  public void deleteByStatDateAndPeriodType(LocalDate date, PeriodType periodType) {
    jpaContentViewStatsRepository.deleteByStatDateAndPeriodType(date, periodType);
  }

  @Override
  public Long getTotalContentViews(List<Long> contentIds, LocalDate startDate, LocalDate endDate) {
    return jpaContentViewStatsRepository.getTotalContentViews(contentIds, startDate, endDate);
  }
}
