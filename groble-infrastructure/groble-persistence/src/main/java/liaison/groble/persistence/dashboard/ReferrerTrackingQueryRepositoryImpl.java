package liaison.groble.persistence.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.dto.FlatReferrerStatsDTO;
import liaison.groble.domain.dashboard.repository.ReferrerTrackingQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReferrerTrackingQueryRepositoryImpl implements ReferrerTrackingQueryRepository {

  private static final String CONTENT_BASE_FILTER =
      " FROM referrer_tracking rt "
          + "WHERE rt.content_id = :contentId "
          + "AND COALESCE(rt.event_timestamp, rt.created_at) >= :start "
          + "AND COALESCE(rt.event_timestamp, rt.created_at) < :end";

  private static final String MARKET_BASE_FILTER =
      " FROM referrer_tracking rt "
          + "WHERE rt.market_link_url = :marketLinkUrl "
          + "AND COALESCE(rt.event_timestamp, rt.created_at) >= :start "
          + "AND COALESCE(rt.event_timestamp, rt.created_at) < :end";

  private static final String DOMAIN_EXPRESSION =
      "CASE WHEN rt.referrer_url IS NULL OR rt.referrer_url = '' THEN '(direct)' "
          + "ELSE LOWER(SUBSTRING_INDEX(SUBSTRING_INDEX(rt.referrer_url, '://', -1), '/', 1)) END";

  private static final String DOMAIN_GROUP_EXPRESSION = "(" + DOMAIN_EXPRESSION + ")";

  private static final String GROUP_BY_COLUMNS =
      " GROUP BY rt.referrer_url, "
          + DOMAIN_GROUP_EXPRESSION
          + ", rt.utm_source, rt.utm_medium, rt.utm_campaign, rt.utm_content, rt.utm_term";

  private static final String DATA_PROJECTION =
      "SELECT rt.referrer_url, "
          + DOMAIN_GROUP_EXPRESSION
          + " AS referrer_domain, "
          + "NULL AS referrer_path, "
          + "rt.utm_source, "
          + "rt.utm_medium, "
          + "rt.utm_campaign, "
          + "rt.utm_content, "
          + "rt.utm_term, "
          + "COUNT(*) AS visit_count";

  private final EntityManager entityManager;

  @Override
  public Page<FlatReferrerStatsDTO> findContentReferrerStats(
      Long contentId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    if (contentId == null) {
      return Page.empty(pageable);
    }

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

    String baseFilter = CONTENT_BASE_FILTER;

    return executeAggregation(
        baseFilter,
        builder -> builder.setParameter("contentId", contentId.toString()),
        startDateTime,
        endExclusive,
        pageable);
  }

  @Override
  public Page<FlatReferrerStatsDTO> findMarketReferrerStats(
      String marketLinkUrl, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

    String baseFilter = MARKET_BASE_FILTER;

    return executeAggregation(
        baseFilter,
        builder -> builder.setParameter("marketLinkUrl", marketLinkUrl),
        startDateTime,
        endExclusive,
        pageable);
  }

  private Page<FlatReferrerStatsDTO> executeAggregation(
      String baseFilter,
      QueryParameterApplier parameterApplier,
      LocalDateTime startDateTime,
      LocalDateTime endExclusive,
      Pageable pageable) {

    String dataQuerySql =
        DATA_PROJECTION + baseFilter + GROUP_BY_COLUMNS + " ORDER BY visit_count DESC";
    Query dataQuery = entityManager.createNativeQuery(dataQuerySql);
    parameterApplier.apply(dataQuery);
    dataQuery.setParameter("start", startDateTime);
    dataQuery.setParameter("end", endExclusive);
    dataQuery.setFirstResult((int) pageable.getOffset());
    dataQuery.setMaxResults(pageable.getPageSize());

    @SuppressWarnings("unchecked")
    List<Object[]> rows = dataQuery.getResultList();
    List<FlatReferrerStatsDTO> content = new ArrayList<>(rows.size());
    for (Object[] row : rows) {
      content.add(
          FlatReferrerStatsDTO.builder()
              .referrerUrl((String) row[0])
              .referrerDomain((String) row[1])
              .referrerPath(null)
              .source((String) row[3])
              .medium((String) row[4])
              .campaign((String) row[5])
              .content((String) row[6])
              .term((String) row[7])
              .visitCount(((Number) row[8]).longValue())
              .build());
    }

    String countQuerySql =
        "SELECT COUNT(*) FROM (SELECT 1" + baseFilter + GROUP_BY_COLUMNS + ") grouped";
    Query countQuery = entityManager.createNativeQuery(countQuerySql);
    parameterApplier.apply(countQuery);
    countQuery.setParameter("start", startDateTime);
    countQuery.setParameter("end", endExclusive);
    Number total = (Number) countQuery.getSingleResult();

    return new PageImpl<>(content, pageable, total.longValue());
  }

  @FunctionalInterface
  private interface QueryParameterApplier {
    void apply(Query query);
  }
}
