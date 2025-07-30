package liaison.groble.persistence.content;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.ContentViewStats;

public interface JpaContentViewStatsRepository extends JpaRepository<ContentViewStats, Long> {
  void deleteByStatDateAndPeriodType(LocalDate date, ContentViewStats.PeriodType periodType);
}
