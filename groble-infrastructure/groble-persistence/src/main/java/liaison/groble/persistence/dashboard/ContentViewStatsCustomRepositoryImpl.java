package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.dto.FlatContentViewStatsDTO;
import liaison.groble.domain.dashboard.entity.QContentViewStats;
import liaison.groble.domain.dashboard.repository.ContentViewStatsCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentViewStatsCustomRepositoryImpl implements ContentViewStatsCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<FlatContentViewStatsDTO> findByContentIdAndPeriodTypeAndStatDateBetween(
      Long contentId,
      PeriodType periodType,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {
    QContentViewStats qContentViewStats = QContentViewStats.contentViewStats;

    // 전체 카운트
    Long total =
        jpaQueryFactory
            .select(qContentViewStats.count())
            .from(qContentViewStats)
            .where(
                qContentViewStats.contentId.eq(contentId),
                qContentViewStats.periodType.eq(periodType),
                qContentViewStats.statDate.between(startDate, endDate))
            .fetchOne();

    // 데이터 조회
    List<FlatContentViewStatsDTO> content =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatContentViewStatsDTO.class,
                    qContentViewStats.statDate,
                    Expressions.constant(""),
                    qContentViewStats.viewCount))
            .from(qContentViewStats)
            .where(
                qContentViewStats.contentId.eq(contentId),
                qContentViewStats.periodType.eq(periodType),
                qContentViewStats.statDate.between(startDate, endDate))
            .orderBy(qContentViewStats.statDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
  }
}
