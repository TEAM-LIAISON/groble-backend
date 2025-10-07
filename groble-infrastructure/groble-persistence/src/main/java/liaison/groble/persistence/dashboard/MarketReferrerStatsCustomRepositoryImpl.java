package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
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

    // 먼저 해당 마켓 ID를 찾기
    Long marketId =
        jpaQueryFactory
            .select(qMarket.id)
            .from(qMarket)
            .where(qMarket.marketLinkUrl.eq(marketLinkUrl))
            .fetchOne();

    if (marketId == null) {
      return new PageImpl<>(List.of(), pageable, 0L);
    }

    // 전체 개수: 마켓의 전체 referrer stats 개수
    Long total =
        jpaQueryFactory
            .select(qStats.id.countDistinct())
            .from(qStats)
            .where(qStats.marketId.eq(marketId))
            .fetchOne();

    // 데이터 조회: visitCount 필드를 직접 사용 (이벤트 기반 집계 대신)
    List<FlatReferrerStatsDTO> content =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatReferrerStatsDTO.class,
                    qStats.referrerUrl,
                    qStats.referrerDomain,
                    qStats.referrerPath,
                    qStats.source,
                    qStats.medium,
                    qStats.campaign,
                    qStats.content,
                    qStats.term,
                    qStats.visitCount.longValue()))
            .from(qStats)
            .where(qStats.marketId.eq(marketId))
            .orderBy(qStats.visitCount.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, (total != null ? total : 0L));
  }
}
