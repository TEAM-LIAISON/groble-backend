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
import liaison.groble.domain.dashboard.entity.QContentReferrerEvent;
import liaison.groble.domain.dashboard.entity.QContentReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReferrerStatsCustomRepositoryImpl
    implements ContentReferrerStatsCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<FlatReferrerStatsDTO> findContentReferrerStats(
      Long contentId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    QContentReferrerStats qStats = QContentReferrerStats.contentReferrerStats;
    QContentReferrerEvent qEvent = QContentReferrerEvent.contentReferrerEvent;

    // [start, end+1day) 형태의 반개구간으로 집계 (between의 양끝 포함 문제 방지)
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

    // 방문수 집계식(재사용)
    NumberExpression<Long> visitCount = qEvent.id.count();

    // 전체 개수: 그룹 기준이 stats.id 이므로 id의 distinct 개수를 사용
    Long total =
        jpaQueryFactory
            .select(qStats.id.countDistinct()) // ❗ 기존 qStats.countDistinct()는 컴파일 오류
            .from(qStats)
            .join(qEvent)
            .on(qEvent.referrerStatsId.eq(qStats.id))
            .where(
                qStats.contentId.eq(contentId),
                qEvent.eventDate.goe(startDateTime),
                qEvent.eventDate.lt(endExclusive))
            .fetchOne();

    // 데이터 조회
    List<FlatReferrerStatsDTO> content =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatReferrerStatsDTO.class,
                    qStats.referrerUrl,
                    visitCount // alias 없이도 생성자 매핑이면 OK
                    ))
            .from(qStats)
            .join(qEvent)
            .on(qEvent.referrerStatsId.eq(qStats.id))
            .where(
                qStats.contentId.eq(contentId),
                qEvent.eventDate.goe(startDateTime),
                qEvent.eventDate.lt(endExclusive))
            // id 단위(=한 referrer 레코드 단위)로 묶고 보조적으로 표시 컬럼도 그룹에 포함
            .groupBy(
                qStats.id,
                qStats.referrerDomain,
                qStats.referrerPath,
                qStats.source,
                qStats.medium,
                qStats.campaign)
            .orderBy(visitCount.desc()) // 재사용한 식으로 정렬
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, (total != null ? total : 0L));
  }
}
