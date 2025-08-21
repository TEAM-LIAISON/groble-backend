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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "market_referrer_stats",
    indexes = {
      @Index(name = "idx_mrs_market_created", columnList = "market_id, created_at"),
      @Index(name = "idx_mrs_source_medium", columnList = "source, medium"),
      @Index(name = "idx_mrs_domain", columnList = "referrer_domain")
    })
@Comment("마켓별 유입 경로 수집 데이터")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class MarketReferrerStats {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "market_id", nullable = false)
  @Comment("마켓 ID")
  private Long marketId;

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

  // 유틸리티 메서드
  public void parseReferrerUrl() {
    if (this.referrerUrl == null || this.referrerUrl.isEmpty()) {
      this.referrerDomain = "(direct)";
      if (this.source == null) this.source = "(direct)";
      if (this.medium == null) this.medium = "(none)";
      return;
    }

    // 도메인 추출
    this.referrerDomain = extractDomain(this.referrerUrl);

    // SNS인 경우 세부 경로 저장
    if (isSocialDomain(this.referrerDomain)) {
      this.referrerPath = extractPath(this.referrerUrl);
    }

    // UTM이 없고 source가 비어있으면 도메인 기반으로 설정
    if ((this.source == null || "(direct)".equals(this.source))
        && !"(direct)".equals(this.referrerDomain)) {
      this.source = mapDomainToSource(this.referrerDomain);
      this.medium =
          this.medium == null || "(none)".equals(this.medium)
              ? (isSocialDomain(this.referrerDomain) ? "social" : "referral")
              : this.medium;
    }
  }

  private String extractDomain(String url) {
    try {
      String clean = url.replaceAll("^https?://", "").replaceAll("^www\\.", "");
      int idx = clean.indexOf('/');
      return idx > 0 ? clean.substring(0, idx).toLowerCase() : clean.toLowerCase();
    } catch (Exception e) {
      return "(unknown)";
    }
  }

  private String extractPath(String url) {
    try {
      String clean = url.replaceAll("^https?://[^/]+", "");
      int idx = clean.indexOf('?');
      return idx > 0 ? clean.substring(0, idx) : clean;
    } catch (Exception e) {
      return "/";
    }
  }

  private boolean isSocialDomain(String domain) {
    return domain != null
        && (domain.contains("instagram.com")
            || domain.contains("threads.com")
            || domain.contains("facebook.com")
            || domain.contains("twitter.com")
            || domain.contains("linkedin.com")
            || domain.contains("youtube.com")
            || domain.contains("tiktok.com"));
  }

  private String mapDomainToSource(String domain) {
    if (domain.contains("instagram.com")) return "instagram";
    if (domain.contains("threads.com")) return "threads";
    if (domain.contains("facebook.com")) return "facebook";
    if (domain.contains("google.com")) return "google";
    if (domain.contains("naver.com")) return "naver";
    return domain.replaceAll("\\.com$|\\.co\\.kr$|\\.net$", "");
  }
}
