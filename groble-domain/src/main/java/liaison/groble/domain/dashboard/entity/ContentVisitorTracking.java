// package liaison.groble.domain.dashboard.entity;
//
// import static jakarta.persistence.GenerationType.IDENTITY;
// import static lombok.AccessLevel.PROTECTED;
//
// import java.time.LocalDate;
// import java.time.LocalDateTime;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EntityListeners;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.Id;
// import jakarta.persistence.Index;
// import jakarta.persistence.Table;
// import jakarta.persistence.UniqueConstraint;
//
// import org.hibernate.annotations.Comment;
// import org.springframework.data.annotation.CreatedDate;
// import org.springframework.data.annotation.LastModifiedDate;
// import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
// import liaison.groble.domain.common.enums.AttributionType;
//
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// @Entity
// @EntityListeners(AuditingEntityListener.class)
// @Table(
//    name = "content_visitor_tracking",
//    uniqueConstraints =
//        @UniqueConstraint(
//            name = "uk_cvt_content_visitor_day",
//            columnNames = {"content_id", "visitor_hash", "tracking_date"}),
//    indexes = {
//      @Index(
//          name = "idx_cvt_aggregate",
//          columnList = "content_id, tracking_date, attributed_domain"),
//      @Index(name = "idx_cvt_visitor", columnList = "content_id, visitor_hash")
//    })
// @Comment("콘텐츠 고유 방문자 추적 및 어트리뷰션")
// @Getter
// @Builder
// @NoArgsConstructor(access = PROTECTED)
// @AllArgsConstructor
// public class ContentVisitorTracking {
//  @Id
//  @GeneratedValue(strategy = IDENTITY)
//  private Long id;
//
//  @Column(name = "content_id", nullable = false)
//  @Comment("콘텐츠 ID")
//  private Long contentId;
//
//  @Column(name = "visitor_hash", nullable = false, length = 64)
//  @Comment("방문자 식별 해시: SHA-256(salt|ip|userAgent)")
//  private String visitorHash;
//
//  @Column(name = "tracking_date", nullable = false)
//  @Comment("추적 일자 (타임존 고정: KST)")
//  private LocalDate trackingDate;
//
//  @Column(name = "attributed_domain", nullable = false, length = 255)
//  @Comment("어트리뷰션 도메인 (정규화: 소문자, www 제거)")
//  private String attributedDomain;
//
//  @Column(name = "attributed_path", length = 2048)
//  @Comment("어트리뷰션 전체 경로 (트래킹 파라미터 제거)")
//  private String attributedPath;
//
//  @Enumerated(EnumType.STRING)
//  @Column(name = "attribution_type", nullable = false, length = 16)
//  @Comment("어트리뷰션 타입: FIRST_TOUCH, LAST_TOUCH")
//  private AttributionType attributionType = AttributionType.FIRST_TOUCH;
//
//  @Column(name = "visit_count", nullable = false, columnDefinition = "int default 1")
//  @Comment("해당 일자 방문 횟수")
//  private Integer visitCount = 1;
//
//  @Column(name = "logged_in", nullable = false, columnDefinition = "tinyint(1) default 0")
//  @Comment("로그인 사용자 여부")
//  private Boolean loggedIn = false;
//
//  @CreatedDate
//  @Column(name = "first_visit_at", nullable = false, updatable = false)
//  @Comment("첫 방문 시각")
//  private LocalDateTime firstVisitAt;
//
//  @LastModifiedDate
//  @Column(name = "last_visit_at", nullable = false)
//  @Comment("마지막 방문 시각")
//  private LocalDateTime lastVisitAt;
// }
