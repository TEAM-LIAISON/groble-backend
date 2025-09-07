package liaison.groble.application.dashboard.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;
import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.entity.MarketReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerEventRepository;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsRepository;
import liaison.groble.domain.market.entity.Market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferrerService {
  private final ContentReferrerStatsRepository contentReferrerStatsRepository;
  private final ContentReferrerEventRepository contentReferrerEventRepository;
  private final MarketReferrerStatsRepository marketReferrerStatsRepository;
  private final UserReader userReader;

  public void recordContentReferrer(Long contentId, ReferrerDTO referrerDTO) {
    try {
      // ContentReferrerStats 찾거나 생성하고 방문수 증가
      ContentReferrerStats stats = findOrCreateContentReferrerStats(contentId, referrerDTO);

      // 방문수 증가
      stats.incrementVisitCount();
      contentReferrerStatsRepository.save(stats);

      // ContentReferrerEvent 생성 및 저장
      ContentReferrerEvent event =
          ContentReferrerEvent.builder()
              .referrerStatsId(stats.getId())
              .contentId(contentId)
              .eventDate(LocalDateTime.now())
              .build();

      contentReferrerEventRepository.save(event);

    } catch (Exception e) {
      log.error("Failed to record content referrer stats for contentId: " + contentId, e);
    }
  }

  public void recordMarketReferrer(String marketLinkUrl, ReferrerDTO referrerDTO) {
    Market market = userReader.getMarketWithUser(marketLinkUrl);

    MarketReferrerStats stats =
        MarketReferrerStats.builder()
            .marketId(market.getId())
            .referrerUrl(referrerDTO.getReferrerUrl())
            .source(referrerDTO.getUtmSource())
            .medium(referrerDTO.getUtmMedium())
            .campaign(referrerDTO.getUtmCampaign())
            .content(referrerDTO.getUtmContent())
            .term(referrerDTO.getUtmTerm())
            .build();
    // referrerUrl 파싱하여 도메인과 경로 추출
    stats.parseReferrerUrl();

    marketReferrerStatsRepository.save(stats);
  }

  private ContentReferrerStats findOrCreateContentReferrerStats(
      Long contentId, ReferrerDTO referrerDTO) {
    // referrerUrl에서 도메인 추출
    String referrerDomain = extractDomainFromUrl(referrerDTO.getReferrerUrl());

    // UTM 파라미터 또는 도메인 기반 값 설정
    String source =
        referrerDTO.getUtmSource() != null
            ? referrerDTO.getUtmSource()
            : mapDomainToSource(referrerDomain);
    String medium =
        referrerDTO.getUtmMedium() != null
            ? referrerDTO.getUtmMedium()
            : inferMediumFromDomain(referrerDomain);
    String campaign = referrerDTO.getUtmCampaign();

    // 기존 통계가 있는지 확인
    Optional<ContentReferrerStats> existing =
        contentReferrerStatsRepository
            .findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
                contentId, referrerDomain, source, medium, campaign);

    if (existing.isPresent()) {
      return existing.get();
    }

    // 새로운 통계 생성
    ContentReferrerStats stats =
        ContentReferrerStats.builder()
            .contentId(contentId)
            .referrerUrl(referrerDTO.getReferrerUrl())
            .referrerDomain(referrerDomain)
            .source(source)
            .medium(medium)
            .campaign(campaign)
            .content(referrerDTO.getUtmContent())
            .term(referrerDTO.getUtmTerm())
            .visitCount(1)
            .build();

    // referrerUrl 파싱하여 추가 정보 설정
    stats.parseReferrerUrl();

    return contentReferrerStatsRepository.save(stats);
  }

  private String extractDomainFromUrl(String url) {
    if (url == null || url.isEmpty()) {
      return "(direct)";
    }
    try {
      String clean = url.replaceAll("^https?://", "").replaceAll("^www\\.", "");
      int idx = clean.indexOf('/');
      return idx > 0 ? clean.substring(0, idx).toLowerCase() : clean.toLowerCase();
    } catch (Exception e) {
      return "(unknown)";
    }
  }

  private String mapDomainToSource(String domain) {
    if (domain == null || "(direct)".equals(domain)) return "(direct)";
    if (domain.contains("instagram.com")) return "instagram";
    if (domain.contains("threads.com")) return "threads";
    if (domain.contains("facebook.com")) return "facebook";
    if (domain.contains("google.com")) return "google";
    if (domain.contains("naver.com")) return "naver";
    return domain.replaceAll("\\.com$|\\.co\\.kr$|\\.net$", "");
  }

  private String inferMediumFromDomain(String domain) {
    if (domain == null || "(direct)".equals(domain)) return "(none)";
    if (isSocialDomain(domain)) return "social";
    if (domain.contains("google.com") || domain.contains("naver.com")) return "search";
    return "referral";
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
}
