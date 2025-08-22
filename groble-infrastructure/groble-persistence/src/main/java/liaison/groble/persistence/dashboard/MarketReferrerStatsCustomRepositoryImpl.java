package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.dashboard.dto.FlatReferrerStatsDTO;
import liaison.groble.domain.dashboard.entity.QMarketReferrerEvent;
import liaison.groble.domain.dashboard.entity.QMarketReferrerStats;
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsCustomRepository;
import liaison.groble.domain.market.entity.QMarket;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MarketReferrerStatsCustomRepositoryImpl
    implements MarketReferrerStatsCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<FlatReferrerStatsDTO> findMarketReferrerStats(
      String marketLinkUrl, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    QMarketReferrerStats qStats = QMarketReferrerStats.marketReferrerStats;
    QMarketReferrerEvent qEvent = QMarketReferrerEvent.marketReferrerEvent;
    QMarket qMarket = QMarket.market;

    // [start, end+1day) 형태의 반개구간으로 집계
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

    // 방문수 집계식
    NumberExpression<Long> visitCount = qEvent.id.count();

    // 전체 개수: 유니크한 referrerUrl 개수
    Long total =
        jpaQueryFactory
            .select(qStats.id.countDistinct())
            .from(qStats)
            .join(qMarket)
            .on(qMarket.id.eq(qStats.marketId))
            .join(qEvent)
            .on(qEvent.referrerStatsId.eq(qStats.id))
            .where(
                qMarket.marketLinkUrl.eq(marketLinkUrl),
                qEvent.eventDate.goe(startDateTime),
                qEvent.eventDate.lt(endExclusive))
            .fetchOne();

    // 데이터 조회
    List<FlatReferrerStatsDTO> content =
        jpaQueryFactory
            .select(
                Projections.constructor(FlatReferrerStatsDTO.class, qStats.referrerUrl, visitCount))
            .from(qStats)
            .join(qMarket)
            .on(qMarket.id.eq(qStats.marketId))
            .join(qEvent)
            .on(qEvent.referrerStatsId.eq(qStats.id))
            .where(
                qMarket.marketLinkUrl.eq(marketLinkUrl),
                qEvent.eventDate.goe(startDateTime),
                qEvent.eventDate.lt(endExclusive))
            .groupBy(qStats.id, qStats.referrerUrl)
            .orderBy(visitCount.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, (total != null ? total : 0L));
  }
}
