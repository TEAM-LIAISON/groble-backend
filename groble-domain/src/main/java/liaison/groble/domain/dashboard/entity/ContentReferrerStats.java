package liaison.groble.domain.dashboard.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import liaison.groble.domain.common.entity.BaseTimeEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "content_referrer_stats",
    indexes = {
      @Index(name = "idx_crs_content_created", columnList = "content_id, created_at"),
      @Index(name = "idx_crs_source_medium", columnList = "source, medium"),
      @Index(name = "idx_crs_domain", columnList = "referrer_domain")
    })
@Comment("콘텐츠별 유입 경로 수집 데이터")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ContentReferrerStats extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "content_id", nullable = false)
  @Comment("콘텐츠 ID")
  private Long contentId;

  // 리퍼러 정보
  @Column(name = "referrer_url", length = 1024)
  @Comment("원본 리퍼러 URL")
  private String referrerUrl;

  @Column(name = "referrer_domain", nullable = false)
  @Comment("리퍼러 도메인 (예: instagram.com, google.com)")
  @Builder.Default
  private String referrerDomain = "(direct)";

  @Column(name = "referrer_path", length = 500)
  @Comment("리퍼러 경로 (SNS 세부 경로)")
  private String referrerPath;

  // UTM 파라미터
  @Column(name = "source", nullable = false, length = 100)
  @Comment("트래픽 소스 (utm_source 또는 도메인 기반)")
  @Builder.Default
  private String source = "(direct)";

  @Column(name = "medium", nullable = false, length = 50)
  @Comment("트래픽 매체 (utm_medium)")
  @Builder.Default
  private String medium = "(none)";

  @Column(name = "campaign")
  @Comment("캠페인명 (utm_campaign)")
  private String campaign;

  @Column(name = "content")
  @Comment("콘텐츠 구분 (utm_content)")
  private String content;

  @Column(name = "term")
  @Comment("검색어 (utm_term)")
  private String term;
}
