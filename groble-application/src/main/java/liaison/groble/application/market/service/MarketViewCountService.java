package liaison.groble.application.market.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.market.dto.MarketViewCountDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.dashboard.entity.MarketReferrerEvent;
import liaison.groble.domain.dashboard.entity.MarketReferrerStats;
import liaison.groble.domain.dashboard.entity.MarketViewLog;
import liaison.groble.domain.dashboard.repository.MarketReferrerEventRepository;
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketViewLogRepository;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.port.DailyViewPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketViewCountService {
  // 관리자 계정 ID 상수
  private static final Long ADMIN_USER_ID = 1L;

  // Reader
  private final UserReader userReader;

  // Repository
  private final MarketViewLogRepository marketViewLogRepository;
  private final MarketReferrerStatsRepository marketReferrerStatsRepository;
  private final MarketReferrerEventRepository marketReferrerEventRepository;

  // Port
  private final DailyViewPort dailyViewPort;

  @Async
  public void recordMarketView(String marketLinkUrl, MarketViewCountDTO marketViewCountDTO) {
    // 관리자 계정(groble@groble.im, userId=1)에 대해서는 조회수 집계를 하지 않음
    if (ADMIN_USER_ID.equals(marketViewCountDTO.getUserId())) {
      return;
    }

    Market market = userReader.getMarketWithUser(marketLinkUrl);

    // # 일별 조회수
    // view:count:market:123:20250128 → "42"

    // # 중복 방지 (1시간)
    // viewed:market:123:user:456 → "1"
    // viewed:market:123:ip:192.168.1.1:382910 → "1"

    String viewerKey =
        marketViewCountDTO.getUserId() != null
            ? "user:" + marketViewCountDTO.getUserId()
            : "ip:"
                + marketViewCountDTO.getIp()
                + ":"
                + marketViewCountDTO.getUserAgent().hashCode();

    if (dailyViewPort.incrementViewIfNotDuplicate("market", market.getId(), viewerKey)) {
      // 로그 저장
      MarketViewLog log =
          MarketViewLog.builder()
              .marketId(market.getId())
              .viewerId(marketViewCountDTO.getUserId())
              .viewerIp(marketViewCountDTO.getIp())
              .userAgent(marketViewCountDTO.getUserAgent())
              .viewedAt(LocalDateTime.now())
              .build();

      marketViewLogRepository.save(log);

      // 리퍼러 통계 처리
      recordReferrerStats(market.getId(), marketViewCountDTO.getReferer());
    }
  }

  private void recordReferrerStats(Long marketId, String referrerUrl) {
    try {
      // MarketReferrerStats 찾거나 생성
      MarketReferrerStats stats = findOrCreateReferrerStats(marketId, referrerUrl);

      // MarketReferrerEvent 생성
      MarketReferrerEvent event =
          MarketReferrerEvent.builder()
              .referrerStatsId(stats.getId())
              .marketId(marketId)
              .eventDate(LocalDateTime.now())
              .build();

      marketReferrerEventRepository.save(event);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private MarketReferrerStats findOrCreateReferrerStats(Long marketId, String referrerUrl) {
    // UTM 파라미터 파싱
    Map<String, String> utmParams = parseUtmParameters(referrerUrl);

    // MarketReferrerStats 생성 또는 조회
    MarketReferrerStats stats =
        MarketReferrerStats.builder()
            .marketId(marketId)
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
    Optional<MarketReferrerStats> existing =
        marketReferrerStatsRepository.findByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
            marketId,
            stats.getReferrerDomain(),
            stats.getSource(),
            stats.getMedium(),
            stats.getCampaign());

    if (existing.isPresent()) {
      return existing.get();
    }

    // 새로운 통계 저장
    return marketReferrerStatsRepository.save(stats);
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
      // 파싱 실패 시 빈 맵 반환
      System.err.println("Failed to parse UTM parameters: " + e.getMessage());
    }

    return params;
  }
}
