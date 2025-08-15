// package liaison.groble.domain.dashboard.entity;
//
// import static jakarta.persistence.EnumType.STRING;
// import static jakarta.persistence.GenerationType.IDENTITY;
// import static lombok.AccessLevel.PROTECTED;
//
// import java.time.LocalDate;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.Id;
// import jakarta.persistence.Index;
// import jakarta.persistence.Table;
// import jakarta.persistence.UniqueConstraint;
//
// import org.hibernate.annotations.Comment;
//
// import liaison.groble.domain.common.entity.BaseTimeEntity;
// import liaison.groble.domain.common.enums.PeriodType;
//
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
/// ** 콘텐츠 유입경로 통계 */
// @Entity
// @Table(
//    name = "content_referrer_stats",
//    uniqueConstraints =
//        @UniqueConstraint(
//            name = "uk_crs_content_date_domain_period",
//            columnNames = {"content_id", "stat_date", "referrer_domain", "period_type"}),
//    indexes = {
//      @Index(name = "idx_crs_lookup", columnList = "content_id, stat_date, period_type"),
//      @Index(name = "idx_crs_domain", columnList = "referrer_domain")
//    })
// @Comment("콘텐츠별 유입 경로 통계")
// @Getter
// @Builder
// @AllArgsConstructor
// @NoArgsConstructor(access = PROTECTED)
// public class ContentReferrerStats extends BaseTimeEntity {
//  @Id
//  @GeneratedValue(strategy = IDENTITY)
//  private Long id;
//
//  @Column(name = "content_id", nullable = false)
//  @Comment("콘텐츠 ID")
//  private Long contentId;
//
//  @Column(name = "stat_date", nullable = false)
//  @Comment("통계 기준 일자")
//  private LocalDate statDate;
//
//  @Enumerated(STRING)
//  @Column(name = "period_type", nullable = false, length = 16)
//  @Comment("집계 기간 유형: DAILY, WEEKLY, MONTHLY")
//  private PeriodType periodType;
//
//  @Column(name = "referrer_domain", nullable = false, length = 255)
//  @Comment("정규화된 유입 도메인 (예: instagram.com, google.com, (direct))")
//  private String referrerDomain;
//
//  @Column(name = "referrer_path", length = 2048)
//  @Comment("선택적: 유입 경로 전체 URL (트래킹 파라미터 제거)")
//  private String referrerPath;
//
//  @Column(name = "view_count", nullable = false, columnDefinition = "bigint default 0")
//  @Comment("총 조회수")
//  private Long viewCount = 0L;
//
//  @Column(name = "unique_visitor_count", nullable = false, columnDefinition = "bigint default 0")
//  @Comment("고유 방문자수 (ContentVisitorTracking 기반 집계)")
//  private Long uniqueVisitorCount = 0L;
// }
