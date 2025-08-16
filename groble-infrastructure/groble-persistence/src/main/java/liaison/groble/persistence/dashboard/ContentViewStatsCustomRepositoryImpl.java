package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.dto.FlatContentTotalViewStatsDTO;
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

  @Override
  public Page<FlatContentTotalViewStatsDTO> findTotalViewsByPeriodTypeAndStatDateBetween(
      PeriodType periodType, LocalDate startDate, LocalDate endDate, Pageable pageable) {

    QContentViewStats stats = QContentViewStats.contentViewStats;

    // 서브쿼리로 그룹화된 결과 카운트
    JPAQuery<Long> countQuery =
        jpaQueryFactory
            .select(stats.contentId)
            .from(stats)
            .where(stats.periodType.eq(periodType), stats.statDate.between(startDate, endDate))
            .groupBy(stats.contentId);

    Long total =
        jpaQueryFactory
            .select(stats.contentId.count())
            .from(stats)
            .where(stats.contentId.in(countQuery))
            .fetchOne();

    // 데이터 조회 - 동적 정렬 처리
    JPAQuery<FlatContentTotalViewStatsDTO> query =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatContentTotalViewStatsDTO.class,
                    stats.contentId,
                    stats.viewCount.sum().coalesce(0L),
                    stats.uniqueViewerCount.sum().coalesce(0L)))
            .from(stats)
            .where(stats.periodType.eq(periodType), stats.statDate.between(startDate, endDate))
            .groupBy(stats.contentId);

    // Pageable의 Sort 적용
    if (pageable.getSort().isSorted()) {
      pageable
          .getSort()
          .forEach(
              order -> {
                if (order.getProperty().equals("viewCount")) {
                  query.orderBy(
                      order.isAscending()
                          ? stats.viewCount.sum().asc()
                          : stats.viewCount.sum().desc());
                }
              });
    } else {
      query.orderBy(stats.viewCount.sum().desc()); // 기본 정렬
    }

    List<FlatContentTotalViewStatsDTO> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
  }
}
