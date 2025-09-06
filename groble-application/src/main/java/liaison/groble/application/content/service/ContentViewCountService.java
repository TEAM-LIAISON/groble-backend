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

      // 리퍼러 통계 처리
      recordReferrerStats(contentId, contentViewCountDTO.getReferer());
    }
  }

  private void recordReferrerStats(Long contentId, String referrerUrl) {
    try {
      // ContentReferrerStats 찾거나 생성
      ContentReferrerStats stats = findOrCreateReferrerStats(contentId, referrerUrl);

      // ContentReferrerEvent 생성
      ContentReferrerEvent event =
          ContentReferrerEvent.builder()
              .referrerStatsId(stats.getId())
              .contentId(contentId)
              .eventDate(LocalDateTime.now())
              .build();

      contentReferrerEventRepository.save(event);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private ContentReferrerStats findOrCreateReferrerStats(Long contentId, String referrerUrl) {
    // UTM 파라미터 파싱
    Map<String, String> utmParams = parseUtmParameters(referrerUrl);

    // ContentReferrerStats 생성 또는 조회
    ContentReferrerStats stats =
        ContentReferrerStats.builder()
            .contentId(contentId)
            .referrerUrl(referrerUrl)
            .source(utmParams.get("utm_source"))
            .medium(utmParams.get("utm_medium"))
            .campaign(utmParams.get("utm_campaign"))
            .content(utmParams.get("utm_content"))
            .term(utmParams.get("utm_term"))
            .build();

    // 리퍼러 URL 파싱 (도메인 추출 등)
    stats.parseReferrerUrl();

    // 기존 통계가 있는지 확인
    Optional<ContentReferrerStats> existing =
        contentReferrerStatsRepository
            .findByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
                contentId,
                stats.getReferrerDomain(),
                stats.getSource(),
                stats.getMedium(),
                stats.getCampaign());

    if (existing.isPresent()) {
      return existing.get();
    }

    // 새로운 통계 저장
    return contentReferrerStatsRepository.save(stats);
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
