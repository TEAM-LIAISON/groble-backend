package liaison.groble.persistence.content;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.entity.ContentViewStats;
import liaison.groble.domain.content.repository.ContentViewStatsRepository;

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
  public void deleteByStatDateAndPeriodType(
      LocalDate date, ContentViewStats.PeriodType periodType) {
    jpaContentViewStatsRepository.deleteByStatDateAndPeriodType(date, periodType);
  }
}
