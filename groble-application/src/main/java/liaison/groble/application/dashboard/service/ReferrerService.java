package liaison.groble.application.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;
import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.entity.MarketReferrerEvent;
import liaison.groble.domain.dashboard.entity.MarketReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerEventRepository;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketReferrerEventRepository;
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
  private final MarketReferrerEventRepository marketReferrerEventRepository;
  private final UserReader userReader;

  public void recordContentReferrer(Long contentId, ReferrerDTO referrerDTO) {
    log.info("=== CONTENT REFERRER DEBUG START ===");
    log.info("ContentId: {}", contentId);
    log.info(
        "Incoming ReferrerDTO: pageUrl={}, referrerUrl={}, utmSource={}, utmMedium={}, utmCampaign={}, utmContent={}, utmTerm={}",
        referrerDTO.getPageUrl(),
        referrerDTO.getReferrerUrl(),
        referrerDTO.getUtmSource(),
        referrerDTO.getUtmMedium(),
        referrerDTO.getUtmCampaign(),
        referrerDTO.getUtmContent(),
        referrerDTO.getUtmTerm());

    try {
      // ContentReferrerStats 찾거나 생성하고 방문수 증가
      ContentReferrerStats stats = findOrCreateContentReferrerStats(contentId, referrerDTO);

      log.info(
          "Final ContentReferrerStats before save: id={}, contentId={}, referrerUrl={}, referrerDomain={}, referrerPath={}, source={}, medium={}, campaign={}, content={}, term={}, visitCount={}",
          stats.getId(),
          stats.getContentId(),
          stats.getReferrerUrl(),
          stats.getReferrerDomain(),
          stats.getReferrerPath(),
          stats.getSource(),
          stats.getMedium(),
          stats.getCampaign(),
          stats.getContent(),
          stats.getTerm(),
          stats.getVisitCount());

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

      log.info("Successfully saved ContentReferrerStats with id: {}", stats.getId());
      log.info("=== CONTENT REFERRER DEBUG END ===");

    } catch (Exception e) {
      log.error("Failed to record content referrer stats for contentId: " + contentId, e);
      log.info("=== CONTENT REFERRER DEBUG END (ERROR) ===");
    }
  }

  public void recordMarketReferrer(String marketLinkUrl, ReferrerDTO referrerDTO) {
    log.info("=== MARKET REFERRER DEBUG START ===");
    log.info("MarketLinkUrl: {}", marketLinkUrl);
    log.info(
        "Incoming ReferrerDTO: pageUrl={}, referrerUrl={}, utmSource={}, utmMedium={}, utmCampaign={}, utmContent={}, utmTerm={}",
        referrerDTO.getPageUrl(),
        referrerDTO.getReferrerUrl(),
        referrerDTO.getUtmSource(),
        referrerDTO.getUtmMedium(),
        referrerDTO.getUtmCampaign(),
        referrerDTO.getUtmContent(),
        referrerDTO.getUtmTerm());

    try {
      Market market = userReader.getMarketWithUser(marketLinkUrl);
      log.info("Found Market: id={}, linkUrl={}", market.getId(), market.getMarketLinkUrl());

      // MarketReferrerStats 찾거나 생성하고 방문수 증가
      MarketReferrerStats stats = findOrCreateMarketReferrerStats(market.getId(), referrerDTO);

      log.info(
          "Final MarketReferrerStats before save: id={}, marketId={}, referrerUrl={}, referrerDomain={}, referrerPath={}, source={}, medium={}, campaign={}, content={}, term={}, visitCount={}",
          stats.getId(),
          stats.getMarketId(),
          stats.getReferrerUrl(),
          stats.getReferrerDomain(),
          stats.getReferrerPath(),
          stats.getSource(),
          stats.getMedium(),
          stats.getCampaign(),
          stats.getContent(),
          stats.getTerm(),
          stats.getVisitCount());

      // 방문수 증가
      stats.incrementVisitCount();
      marketReferrerStatsRepository.save(stats);

      // MarketReferrerEvent 생성 및 저장
      MarketReferrerEvent event =
          MarketReferrerEvent.builder()
              .referrerStatsId(stats.getId())
              .marketId(market.getId())
              .eventDate(LocalDateTime.now())
              .build();

      marketReferrerEventRepository.save(event);

      log.info("Successfully saved MarketReferrerStats with id: {}", stats.getId());
      log.info("=== MARKET REFERRER DEBUG END ===");

    } catch (Exception e) {
      log.error("Failed to record market referrer stats for marketLinkUrl: " + marketLinkUrl, e);
      log.info("=== MARKET REFERRER DEBUG END (ERROR) ===");
    }
  }

  private ContentReferrerStats findOrCreateContentReferrerStats(
      Long contentId, ReferrerDTO referrerDTO) {
    log.info("--- findOrCreateContentReferrerStats START ---");

    // referrerUrl에서 도메인 추출
    String referrerDomain = extractDomainFromUrl(referrerDTO.getReferrerUrl());
    log.info(
        "Extracted referrerDomain: {} from referrerUrl: {}",
        referrerDomain,
        referrerDTO.getReferrerUrl());

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

    log.info(
        "Computed values: source={} (from UTM: {}), medium={} (from UTM: {}), campaign={}",
        source,
        referrerDTO.getUtmSource(),
        medium,
        referrerDTO.getUtmMedium(),
        campaign);

    // 기존 통계가 있는지 확인 - 중복 데이터 처리
    List<ContentReferrerStats> existingList =
        contentReferrerStatsRepository
            .findAllByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
                contentId, referrerDomain, source, medium, campaign);

    if (!existingList.isEmpty()) {
      // 중복 데이터가 있는 경우 처리
      if (existingList.size() > 1) {
        log.warn(
            "Found {} duplicate ContentReferrerStats for contentId: {}, consolidating...",
            existingList.size(),
            contentId);

        // 가장 오래된 것을 기준으로 하고 나머지는 삭제
        ContentReferrerStats primary = existingList.get(0);
        for (int i = 1; i < existingList.size(); i++) {
          ContentReferrerStats duplicate = existingList.get(i);
          // 방문 카운트를 합산
          primary.consolidateVisitCount(duplicate.getVisitCount());
          // 중복 데이터 삭제
          contentReferrerStatsRepository.delete(duplicate);
          log.info("Deleted duplicate ContentReferrerStats with id: {}", duplicate.getId());
        }
        contentReferrerStatsRepository.save(primary);
        log.info("Consolidated visits into ContentReferrerStats with id: {}", primary.getId());
        log.info("--- findOrCreateContentReferrerStats END (CONSOLIDATED) ---");
        return primary;
      }

      ContentReferrerStats existing = existingList.get(0);
      log.info("Found existing ContentReferrerStats with id: {}", existing.getId());
      log.info("--- findOrCreateContentReferrerStats END (EXISTING) ---");
      return existing;
    }

    log.info("No existing stats found, creating new one...");

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

    log.info(
        "Built ContentReferrerStats before parseReferrerUrl: referrerUrl={}, referrerDomain={}, source={}, medium={}, campaign={}, content={}, term={}",
        stats.getReferrerUrl(),
        stats.getReferrerDomain(),
        stats.getSource(),
        stats.getMedium(),
        stats.getCampaign(),
        stats.getContent(),
        stats.getTerm());

    // referrerUrl 파싱하여 추가 정보 설정
    stats.parseReferrerUrl();

    log.info(
        "After parseReferrerUrl: referrerDomain={}, referrerPath={}, source={}, medium={}",
        stats.getReferrerDomain(),
        stats.getReferrerPath(),
        stats.getSource(),
        stats.getMedium());

    ContentReferrerStats savedStats = contentReferrerStatsRepository.save(stats);
    log.info("Saved new ContentReferrerStats with id: {}", savedStats.getId());
    log.info("--- findOrCreateContentReferrerStats END (NEW) ---");

    return savedStats;
  }

  private MarketReferrerStats findOrCreateMarketReferrerStats(
      Long marketId, ReferrerDTO referrerDTO) {
    log.info("--- findOrCreateMarketReferrerStats START ---");

    // referrerUrl에서 도메인 추출
    String referrerDomain = extractDomainFromUrl(referrerDTO.getReferrerUrl());
    log.info(
        "Extracted referrerDomain: {} from referrerUrl: {}",
        referrerDomain,
        referrerDTO.getReferrerUrl());

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

    log.info(
        "Computed values: source={} (from UTM: {}), medium={} (from UTM: {}), campaign={}",
        source,
        referrerDTO.getUtmSource(),
        medium,
        referrerDTO.getUtmMedium(),
        campaign);

    // 기존 통계가 있는지 확인 - 중복 데이터 처리
    List<MarketReferrerStats> existingList =
        marketReferrerStatsRepository
            .findAllByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
                marketId, referrerDomain, source, medium, campaign);

    if (!existingList.isEmpty()) {
      // 중복 데이터가 있는 경우 처리
      if (existingList.size() > 1) {
        log.warn(
            "Found {} duplicate MarketReferrerStats for marketId: {}, consolidating...",
            existingList.size(),
            marketId);

        // 가장 오래된 것을 기준으로 하고 나머지는 삭제
        MarketReferrerStats primary = existingList.get(0);
        for (int i = 1; i < existingList.size(); i++) {
          MarketReferrerStats duplicate = existingList.get(i);
          // 방문 카운트를 합산
          primary.consolidateVisitCount(duplicate.getVisitCount());
          // 중복 데이터 삭제
          marketReferrerStatsRepository.delete(duplicate);
          log.info("Deleted duplicate MarketReferrerStats with id: {}", duplicate.getId());
        }
        marketReferrerStatsRepository.save(primary);
        log.info("Consolidated visits into MarketReferrerStats with id: {}", primary.getId());
        log.info("--- findOrCreateMarketReferrerStats END (CONSOLIDATED) ---");
        return primary;
      }

      MarketReferrerStats existing = existingList.get(0);
      log.info("Found existing MarketReferrerStats with id: {}", existing.getId());
      log.info("--- findOrCreateMarketReferrerStats END (EXISTING) ---");
      return existing;
    }

    log.info("No existing stats found, creating new one...");

    // 새로운 통계 생성
    MarketReferrerStats stats =
        MarketReferrerStats.builder()
            .marketId(marketId)
            .referrerUrl(referrerDTO.getReferrerUrl())
            .referrerDomain(referrerDomain)
            .source(source)
            .medium(medium)
            .campaign(campaign)
            .content(referrerDTO.getUtmContent())
            .term(referrerDTO.getUtmTerm())
            .visitCount(1)
            .build();

    log.info(
        "Built MarketReferrerStats before parseReferrerUrl: referrerUrl={}, referrerDomain={}, source={}, medium={}, campaign={}, content={}, term={}",
        stats.getReferrerUrl(),
        stats.getReferrerDomain(),
        stats.getSource(),
        stats.getMedium(),
        stats.getCampaign(),
        stats.getContent(),
        stats.getTerm());

    // referrerUrl 파싱하여 추가 정보 설정
    stats.parseReferrerUrl();

    log.info(
        "After parseReferrerUrl: referrerDomain={}, referrerPath={}, source={}, medium={}",
        stats.getReferrerDomain(),
        stats.getReferrerPath(),
        stats.getSource(),
        stats.getMedium());

    MarketReferrerStats savedStats = marketReferrerStatsRepository.save(stats);
    log.info("Saved new MarketReferrerStats with id: {}", savedStats.getId());
    log.info("--- findOrCreateMarketReferrerStats END (NEW) ---");

    return savedStats;
  }

  private String extractDomainFromUrl(String url) {
    log.debug("extractDomainFromUrl input: {}", url);
    if (url == null || url.isEmpty()) {
      log.debug("extractDomainFromUrl output: (direct) - null/empty input");
      return "(direct)";
    }
    try {
      String clean = url.replaceAll("^https?://", "").replaceAll("^www\\.", "");
      int idx = clean.indexOf('/');
      String result = idx > 0 ? clean.substring(0, idx).toLowerCase() : clean.toLowerCase();
      log.debug("extractDomainFromUrl output: {} - from clean: {}", result, clean);
      return result;
    } catch (Exception e) {
      log.debug("extractDomainFromUrl output: (unknown) - exception: {}", e.getMessage());
      return "(unknown)";
    }
  }

  private String mapDomainToSource(String domain) {
    log.debug("mapDomainToSource input: {}", domain);
    String result;
    if (domain == null || "(direct)".equals(domain)) result = "(direct)";
    else if (domain.contains("instagram.com")) result = "instagram";
    else if (domain.contains("threads.com")) result = "threads";
    else if (domain.contains("facebook.com")) result = "facebook";
    else if (domain.contains("google.com")) result = "google";
    else if (domain.contains("naver.com")) result = "naver";
    else result = domain.replaceAll("\\.com$|\\.co\\.kr$|\\.net$", "");

    log.debug("mapDomainToSource output: {}", result);
    return result;
  }

  private String inferMediumFromDomain(String domain) {
    log.debug("inferMediumFromDomain input: {}", domain);
    String result;
    if (domain == null || "(direct)".equals(domain)) result = "(none)";
    else if (isSocialDomain(domain)) result = "social";
    else if (domain.contains("google.com") || domain.contains("naver.com")) result = "search";
    else result = "referral";

    log.debug("inferMediumFromDomain output: {}", result);
    return result;
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
