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

    // [start, end+1day) 형태의 반개구간으로 집계
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

    // contentId가 null인 경우 빈 결과 반환
    if (contentId == null) {
      return new PageImpl<>(List.of(), pageable, 0L);
    }

    // 전체 개수: 콘텐츠의 전체 referrer stats 개수
    Long total =
        jpaQueryFactory
            .select(qStats.id.countDistinct())
            .from(qStats)
            .where(qStats.contentId.eq(contentId))
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
            .where(qStats.contentId.eq(contentId))
            .orderBy(qStats.visitCount.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, (total != null ? total : 0L));
  }
}
