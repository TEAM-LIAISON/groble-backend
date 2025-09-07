package liaison.groble.application.content.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.content.dto.ContentViewCountDTO;
import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;
import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.entity.ContentViewLog;
import liaison.groble.domain.dashboard.repository.ContentReferrerEventRepository;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.ContentViewLogRepository;
import liaison.groble.domain.port.DailyViewPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentViewCountService {
  // 관리자 계정 ID 상수
  private static final Long ADMIN_USER_ID = 1L;

  // Repository
  private final ContentViewLogRepository contentViewLogRepository;
  private final ContentReferrerStatsRepository contentReferrerStatsRepository;
  private final ContentReferrerEventRepository contentReferrerEventRepository;

  // Port
  private final DailyViewPort dailyViewPort;

  @Async
  public void recordContentView(Long contentId, ContentViewCountDTO contentViewCountDTO) {
    // 관리자 계정(groble@groble.im, userId=1)에 대해서는 조회수 집계를 하지 않음
    if (ADMIN_USER_ID.equals(contentViewCountDTO.getUserId())) {
      return;
    }
    // # 일별 조회수
    // view:count:content:123:20250128 → "42"

    // # 중복 방지 (1시간)
    // viewed:content:123:user:456 → "1"
    // viewed:content:123:ip:192.168.1.1:382910 → "1"
    String viewerKey =
        contentViewCountDTO.getUserId() != null
            ? "user:" + contentViewCountDTO.getUserId()
            : "ip:"
                + contentViewCountDTO.getIp()
                + ":"
                + contentViewCountDTO.getUserAgent().hashCode();

    if (dailyViewPort.incrementViewIfNotDuplicate("content", contentId, viewerKey)) {
      // 로그 저장
      ContentViewLog log =
          ContentViewLog.builder()
              .contentId(contentId)
              .viewerId(contentViewCountDTO.getUserId())
              .viewerIp(contentViewCountDTO.getIp())
              .userAgent(contentViewCountDTO.getUserAgent())
              .viewedAt(LocalDateTime.now())
              .build();

      contentViewLogRepository.save(log);
    }
  }

  private void recordReferrerStats(Long contentId, String referrerUrl) {
    try {
      // ContentReferrerStats 찾거나 생성하고 방문수 증가
      ContentReferrerStats stats = findOrCreateReferrerStats(contentId, referrerUrl);

      // 방문수 증가
      stats.incrementVisitCount();
      contentReferrerStatsRepository.save(stats);

      ContentReferrerEvent event =
          ContentReferrerEvent.builder()
              .referrerStatsId(stats.getId())
              .contentId(contentId)
              .eventDate(LocalDateTime.now())
              .build();

      contentReferrerEventRepository.save(event);

    } catch (Exception e) {
      log.error("Failed to record referrer stats for contentId: " + contentId, e);
    }
  }

  private ContentReferrerStats findOrCreateReferrerStats(Long contentId, String referrerUrl) {
    // UTM 파라미터 파싱
    Map<String, String> utmParams = parseUtmParameters(referrerUrl);

    // 먼저 간단한 키로 조회 시도 (캐시 활용 가능)
    String referrerDomain = extractDomainFromUrl(referrerUrl);
    String source = utmParams.getOrDefault("utm_source", mapDomainToSource(referrerDomain));
    String medium = utmParams.getOrDefault("utm_medium", inferMediumFromDomain(referrerDomain));
    String campaign = utmParams.get("utm_campaign");

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
            .referrerUrl(referrerUrl)
            .referrerDomain(referrerDomain)
            .source(source)
            .medium(medium)
            .campaign(campaign)
            .content(utmParams.get("utm_content"))
            .term(utmParams.get("utm_term"))
            .visitCount(1) // 초기값 설정
            .build();

    // 추가적인 파싱이 필요한 경우만 실행
    stats.parseReferrerUrl();

    // 새로운 통계 저장
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

  private Map<String, String> parseUtmParameters(String url) {
    Map<String, String> params = new HashMap<>();
    if (url == null || url.isEmpty()) {
      return params;
    }

    try {
      // URL에서 쿼리 파라미터 추출
      int queryStart = url.indexOf('?');
      if (queryStart == -1) {
        return params;
      }

      String query = url.substring(queryStart + 1);
      String[] pairs = query.split("&");

      for (String pair : pairs) {
        String[] keyValue = pair.split("=", 2);
        if (keyValue.length == 2) {
          String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
          String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

          if (key.startsWith("utm_")) {
            params.put(key, value);
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to parse UTM parameters: " + e.getMessage());
    }

    return params;
  }
}
