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
import liaison.groble.domain.dashboard.dto.FlatMarketViewStatsDTO;
import liaison.groble.domain.dashboard.entity.QMarketViewStats;
import liaison.groble.domain.dashboard.repository.MarketViewStatsCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MarketViewStatsCustomRepositoryImpl implements MarketViewStatsCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<FlatMarketViewStatsDTO> findByMarketIdAndPeriodTypeAndStatDateBetween(
      Long marketId,
      PeriodType periodType,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {
    QMarketViewStats qMarketViewStats = QMarketViewStats.marketViewStats;

    // 전체 카운트
    Long total =
        jpaQueryFactory
            .select(qMarketViewStats.count())
            .from(qMarketViewStats)
            .where(
                qMarketViewStats.marketId.eq(marketId),
                qMarketViewStats.periodType.eq(periodType),
                qMarketViewStats.statDate.between(startDate, endDate))
            .fetchOne();

    // 데이터 조회
    List<FlatMarketViewStatsDTO> content =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatMarketViewStatsDTO.class,
                    qMarketViewStats.statDate,
                    Expressions.constant(""),
                    qMarketViewStats.viewCount))
            .from(qMarketViewStats)
            .where(
                qMarketViewStats.marketId.eq(marketId),
                qMarketViewStats.periodType.eq(periodType),
                qMarketViewStats.statDate.between(startDate, endDate))
            .orderBy(qMarketViewStats.statDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
  }
}
