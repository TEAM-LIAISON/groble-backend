package liaison.groble.domain.dashboard.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

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

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Table(
    name = "referrer_tracking",
    indexes = {
      @Index(name = "idx_referrer_tracking_content_id", columnList = "content_id"),
      @Index(name = "idx_referrer_tracking_market_link_url", columnList = "market_link_url"),
      @Index(name = "idx_referrer_tracking_session_id", columnList = "session_id"),
      @Index(name = "idx_referrer_tracking_created_at", columnList = "created_at"),
      @Index(name = "idx_referrer_tracking_utm_source", columnList = "utm_source"),
      @Index(name = "idx_referrer_tracking_utm_campaign", columnList = "utm_campaign")
    })
@Comment("콘텐츠/마켓 유입 경로 원본 추적 데이터")
public class ReferrerTracking extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "content_id", length = 255)
  @Comment("콘텐츠 ID (문자열 저장)")
  private String contentId;

  @Column(name = "market_link_url", length = 500)
  @Comment("마켓 링크 URL")
  private String marketLinkUrl;

  @Column(name = "page_url", nullable = false, length = 500)
  @Comment("현재 페이지 URL")
  private String pageUrl;

  @Column(name = "referrer_url", length = 500)
  @Comment("직전 페이지 URL")
  private String referrerUrl;

  @Column(name = "referrer_domain", length = 255)
  @Comment("직전 페이지 도메인")
  private String referrerDomain;

  @Column(name = "utm_source", length = 255)
  @Comment("UTM 소스")
  private String utmSource;

  @Column(name = "utm_medium", length = 255)
  @Comment("UTM 매체")
  private String utmMedium;

  @Column(name = "utm_campaign", length = 255)
  @Comment("UTM 캠페인")
  private String utmCampaign;

  @Column(name = "utm_content", length = 255)
  @Comment("UTM 콘텐츠")
  private String utmContent;

  @Column(name = "utm_term", length = 255)
  @Comment("UTM 키워드")
  private String utmTerm;

  @Column(name = "landing_page_url", length = 500)
  @Comment("최초 진입 페이지")
  private String landingPageUrl;

  @Column(name = "last_page_url", length = 500)
  @Comment("직전 페이지 URL")
  private String lastPageUrl;

  @Column(name = "referrer_chain", columnDefinition = "json")
  @Comment("유입 경로 체인 JSON")
  private String referrerChain;

  @Column(name = "referrer_metadata", columnDefinition = "json")
  @Comment("추가 리퍼러 메타데이터 JSON")
  private String referrerMetadata;

  @Column(name = "session_id", length = 255)
  @Comment("세션 ID")
  private String sessionId;

  @Column(name = "user_agent", columnDefinition = "text")
  @Comment("사용자 에이전트")
  private String userAgent;

  @Column(name = "ip_address", length = 45)
  @Comment("IP 주소 (마스킹)")
  private String ipAddress;

  @Column(name = "event_timestamp")
  @Comment("클라이언트 이벤트 타임스탬프")
  private LocalDateTime eventTimestamp;

  public void refreshTracking(
      String pageUrl,
      String referrerUrl,
      String utmSource,
      String utmMedium,
      String utmCampaign,
      String utmContent,
      String utmTerm,
      String landingPageUrl,
      String lastPageUrl,
      String referrerChain,
      String referrerMetadata,
      String referrerDomain,
      String userAgent,
      String ipAddress,
      LocalDateTime eventTimestamp) {
    this.pageUrl = pageUrl;
    this.referrerUrl = referrerUrl;
    this.referrerDomain = referrerDomain;
    this.utmSource = utmSource;
    this.utmMedium = utmMedium;
    this.utmCampaign = utmCampaign;
    this.utmContent = utmContent;
    this.utmTerm = utmTerm;
    this.landingPageUrl = landingPageUrl;
    this.lastPageUrl = lastPageUrl;
    this.referrerChain = referrerChain;
    this.referrerMetadata = referrerMetadata;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.eventTimestamp = eventTimestamp;
  }

  public static ReferrerTracking forContent(
      Long contentId,
      String pageUrl,
      String referrerUrl,
      String utmSource,
      String utmMedium,
      String utmCampaign,
      String utmContent,
      String utmTerm,
      String landingPageUrl,
      String lastPageUrl,
      String referrerChain,
      String referrerMetadata,
      String sessionId,
      String referrerDomain,
      String userAgent,
      String ipAddress,
      LocalDateTime eventTimestamp) {
    return ReferrerTracking.builder()
        .contentId(contentId == null ? null : contentId.toString())
        .pageUrl(pageUrl)
        .referrerUrl(referrerUrl)
        .referrerDomain(referrerDomain)
        .utmSource(utmSource)
        .utmMedium(utmMedium)
        .utmCampaign(utmCampaign)
        .utmContent(utmContent)
        .utmTerm(utmTerm)
        .landingPageUrl(landingPageUrl)
        .lastPageUrl(lastPageUrl)
        .referrerChain(referrerChain)
        .referrerMetadata(referrerMetadata)
        .sessionId(sessionId)
        .userAgent(userAgent)
        .ipAddress(ipAddress)
        .eventTimestamp(eventTimestamp)
        .build();
  }

  public static ReferrerTracking forMarket(
      String marketLinkUrl,
      String pageUrl,
      String referrerUrl,
      String utmSource,
      String utmMedium,
      String utmCampaign,
      String utmContent,
      String utmTerm,
      String landingPageUrl,
      String lastPageUrl,
      String referrerChain,
      String referrerMetadata,
      String sessionId,
      String referrerDomain,
      String userAgent,
      String ipAddress,
      LocalDateTime eventTimestamp) {
    return ReferrerTracking.builder()
        .marketLinkUrl(marketLinkUrl)
        .pageUrl(pageUrl)
        .referrerUrl(referrerUrl)
        .referrerDomain(referrerDomain)
        .utmSource(utmSource)
        .utmMedium(utmMedium)
        .utmCampaign(utmCampaign)
        .utmContent(utmContent)
        .utmTerm(utmTerm)
        .landingPageUrl(landingPageUrl)
        .lastPageUrl(lastPageUrl)
        .referrerChain(referrerChain)
        .referrerMetadata(referrerMetadata)
        .sessionId(sessionId)
        .userAgent(userAgent)
        .ipAddress(ipAddress)
        .eventTimestamp(eventTimestamp)
        .build();
  }
}
