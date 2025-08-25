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
import liaison.groble.domain.content.entity.QContent;
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
      Long userId, // 사용자 ID 파라미터 추가
      PeriodType periodType,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {

    QContentViewStats qContentViewStats = QContentViewStats.contentViewStats;
    QContent qContent = QContent.content;

    // 1. 전체 콘텐츠 수 조회 (사용자가 소유한 모든 콘텐츠)
    Long total =
        jpaQueryFactory
            .select(qContent.count())
            .from(qContent)
            .where(qContent.user.id.eq(userId))
            .fetchOne();

    // 2. 데이터 조회 - Content를 기준으로 LEFT JOIN
    JPAQuery<FlatContentTotalViewStatsDTO> query =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatContentTotalViewStatsDTO.class,
                    qContent.id,
                    qContent.title,
                    qContentViewStats.viewCount.sum().coalesce(0L) // null인 경우 0으로 처리
                    ))
            .from(qContent)
            .leftJoin(qContentViewStats)
            .on(
                qContent
                    .id
                    .eq(qContentViewStats.contentId)
                    .and(qContentViewStats.periodType.eq(periodType))
                    .and(qContentViewStats.statDate.between(startDate, endDate)))
            .where(qContent.user.id.eq(userId))
            .groupBy(qContent.id, qContent.title);

    // 3. 정렬 처리
    if (pageable.getSort().isSorted()) {
      pageable
          .getSort()
          .forEach(
              order -> {
                if (order.getProperty().equals("viewCount")) {
                  // 조회수 정렬
                  query.orderBy(
                      order.isAscending()
                          ? qContentViewStats.viewCount.sum().coalesce(0L).asc()
                          : qContentViewStats.viewCount.sum().coalesce(0L).desc());
                } else if (order.getProperty().equals("title")) {
                  // 제목 정렬
                  query.orderBy(order.isAscending() ? qContent.title.asc() : qContent.title.desc());
                }
              });
    } else {
      // 기본 정렬: 조회수 내림차순, 같으면 제목 오름차순
      query.orderBy(qContentViewStats.viewCount.sum().coalesce(0L).desc(), qContent.title.asc());
    }

    // 4. 페이징 처리
    List<FlatContentTotalViewStatsDTO> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
  }
}
