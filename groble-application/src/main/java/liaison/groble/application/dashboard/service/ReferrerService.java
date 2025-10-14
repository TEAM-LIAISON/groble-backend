package liaison.groble.application.dashboard.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;
import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.entity.MarketReferrerEvent;
import liaison.groble.domain.dashboard.entity.MarketReferrerStats;
import liaison.groble.domain.dashboard.entity.ReferrerTracking;
import liaison.groble.domain.dashboard.repository.ContentReferrerEventRepository;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketReferrerEventRepository;
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.ReferrerTrackingRepository;
import liaison.groble.domain.dashboard.support.ReferrerDomainUtils;
import liaison.groble.domain.market.entity.Market;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferrerService {
  private static final String METRIC_NAME = "referrer.tracking.events";
  private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

  private final ContentReferrerStatsRepository contentReferrerStatsRepository;
  private final ContentReferrerEventRepository contentReferrerEventRepository;
  private final MarketReferrerStatsRepository marketReferrerStatsRepository;
  private final MarketReferrerEventRepository marketReferrerEventRepository;
  private final ReferrerTrackingRepository referrerTrackingRepository;
  private final UserReader userReader;
  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;

  public void recordContentReferrer(
      Long contentId,
      ReferrerDTO referrerDTO,
      String refererHeader,
      String userAgent,
      String clientIp) {
    if (referrerDTO == null) {
      log.warn("Received null ReferrerDTO for contentId={}.", contentId);
      return;
    }

    boolean persisted;
    try {
      persisted =
          persistContentReferrerTracking(
              contentId, referrerDTO, refererHeader, userAgent, clientIp);
    } catch (Exception e) {
      log.error("Failed to persist content referrer tracking for contentId={}", contentId, e);
      recordMetric("content", "error");
      return;
    }

    if (!persisted) {
      return;
    }

    try {
      ContentReferrerStats stats = findOrCreateContentReferrerStats(contentId, referrerDTO);
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
      log.error("Failed to record content referrer stats for contentId: " + contentId, e);
    }
  }

  public void recordMarketReferrer(
      String marketLinkUrl,
      ReferrerDTO referrerDTO,
      String refererHeader,
      String userAgent,
      String clientIp) {
    if (referrerDTO == null) {
      log.warn("Received null ReferrerDTO for marketLinkUrl={}.", marketLinkUrl);
      return;
    }

    log.info("=== MARKET REFERRER DEBUG START ===");
    log.info("MarketLinkUrl: {}", marketLinkUrl);
    log.info(
        "Incoming ReferrerDTO: pageUrl={}, referrerUrl={}, utmSource={}, utmMedium={}, utmCampaign={}, utmContent={}, utmTerm={}, landingPage={}, lastPage={}, sessionId={}",
        referrerDTO.getPageUrl(),
        referrerDTO.getReferrerUrl(),
        referrerDTO.getUtmSource(),
        referrerDTO.getUtmMedium(),
        referrerDTO.getUtmCampaign(),
        referrerDTO.getUtmContent(),
        referrerDTO.getUtmTerm(),
        referrerDTO.getLandingPageUrl(),
        referrerDTO.getLastPageUrl(),
        referrerDTO.getSessionId());

    boolean persisted;
    try {
      persisted =
          persistMarketReferrerTracking(
              marketLinkUrl, referrerDTO, refererHeader, userAgent, clientIp);
    } catch (Exception e) {
      log.error(
          "Failed to persist market referrer tracking for marketLinkUrl={}", marketLinkUrl, e);
      recordMetric("market", "error");
      log.info("=== MARKET REFERRER DEBUG END (ERROR) ===");
      return;
    }

    if (!persisted) {
      log.info(
          "Skipping market referrer stats for marketLinkUrl={} due to filtered tracking.",
          marketLinkUrl);
      log.info("=== MARKET REFERRER DEBUG END (SKIPPED) ===");
      return;
    }

    try {
      Market market = userReader.getMarketWithUser(marketLinkUrl);
      log.info("Found Market: id={}, linkUrl={}", market.getId(), market.getMarketLinkUrl());

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

      stats.incrementVisitCount();
      marketReferrerStatsRepository.save(stats);

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

  public long purgeTrackingOlderThanOneYear() {
    LocalDateTime threshold = LocalDateTime.now().minusYears(1);
    long removed = referrerTrackingRepository.deleteAllByCreatedAtBefore(threshold);
    if (removed > 0) {
      log.info("Purged {} referrer tracking records older than {}", removed, threshold);
      meterRegistry
          .counter(METRIC_NAME, "type", "maintenance", "outcome", "purged")
          .increment(removed);
    }
    return removed;
  }

  private boolean persistContentReferrerTracking(
      Long contentId,
      ReferrerDTO referrerDTO,
      String refererHeader,
      String userAgent,
      String clientIp)
      throws JsonProcessingException {
    if (!StringUtils.hasText(referrerDTO.getSessionId())) {
      log.debug("Skip content tracking due to missing sessionId. contentId={}", contentId);
      recordMetric("content", "invalid_session");
      return false;
    }

    String contentIdStr = contentId == null ? null : contentId.toString();
    String resolvedReferrerUrl =
        normalizeReferrerUrl(
            resolveReferrerUrl(referrerDTO, refererHeader), referrerDTO.getPageUrl());
    String chainJson = toReferrerChainJson(referrerDTO.getReferrerChain());
    String metadataJson = toMetadataJson(referrerDTO);
    String maskedIp = maskIpAddress(clientIp);
    String sanitizedUserAgent = sanitizeUserAgent(resolveUserAgent(userAgent, referrerDTO));
    LocalDateTime eventTimestamp = defaultEventTimestamp(referrerDTO.getTimestamp());

    String chainFallback = lastElement(referrerDTO.getReferrerChain());
    if (!StringUtils.hasText(resolvedReferrerUrl) && StringUtils.hasText(chainFallback)) {
      resolvedReferrerUrl = normalizeReferrerUrl(chainFallback, referrerDTO.getPageUrl());
    }

    String firstReferrerUrl = referrerDTO.getFirstReferrerUrl();
    if (!StringUtils.hasText(resolvedReferrerUrl) && StringUtils.hasText(firstReferrerUrl)) {
      resolvedReferrerUrl = normalizeReferrerUrl(firstReferrerUrl, referrerDTO.getPageUrl());
    }

    String referrerDomain = resolveReferrerDomain(resolvedReferrerUrl);
    String lastPageUrl = referrerDTO.getLastPageUrl();
    if (!StringUtils.hasText(referrerDomain) && StringUtils.hasText(lastPageUrl)) {
      resolvedReferrerUrl = normalizeReferrerUrl(lastPageUrl, referrerDTO.getPageUrl());
      referrerDomain = resolveReferrerDomain(resolvedReferrerUrl);
    }

    if (ReferrerDomainUtils.isInternalDomain(referrerDomain)
        && StringUtils.hasText(referrerDTO.getSessionId())) {
      final String sessionId = referrerDTO.getSessionId();
      final String pageUrl = referrerDTO.getPageUrl();
      final String[] resolvedHolder = {resolvedReferrerUrl};
      final String[] domainHolder = {referrerDomain};

      referrerTrackingRepository
          .findLatestMarketNavigation(sessionId)
          .ifPresent(
              recentMarket -> {
                if (!StringUtils.hasText(recentMarket.getPageUrl())) {
                  return;
                }
                String candidate = normalizeReferrerUrl(recentMarket.getPageUrl(), pageUrl);
                String candidateDomain = resolveReferrerDomain(candidate);
                if (StringUtils.hasText(candidateDomain)) {
                  resolvedHolder[0] = candidate;
                  domainHolder[0] = candidateDomain;
                }
              });

      resolvedReferrerUrl = resolvedHolder[0];
      referrerDomain = domainHolder[0];
    }

    if (StringUtils.hasText(referrerDomain) && referrerDomain.contains("admin.groble.im")) {
      log.debug("Skip admin referral for contentId={}.", contentId);
      recordMetric("content", "ignored_admin");
      return false;
    }

    Optional<ReferrerTracking> existing =
        referrerTrackingRepository.findRecentContentTracking(
            referrerDTO.getSessionId(), contentIdStr);

    if (existing.isPresent()) {
      ReferrerTracking tracking = existing.get();
      if (isDuplicateTracking(
          tracking,
          referrerDTO,
          chainJson,
          metadataJson,
          resolvedReferrerUrl,
          referrerDomain,
          eventTimestamp,
          sanitizedUserAgent,
          maskedIp)) {
        log.debug(
            "Detected duplicate content tracking. contentId={}, sessionId={}, pageUrl={}",
            contentId,
            referrerDTO.getSessionId(),
            referrerDTO.getPageUrl());
        recordMetric("content", "duplicate");
        return false;
      }

      tracking.refreshTracking(
          referrerDTO.getPageUrl(),
          resolvedReferrerUrl,
          referrerDTO.getUtmSource(),
          referrerDTO.getUtmMedium(),
          referrerDTO.getUtmCampaign(),
          referrerDTO.getUtmContent(),
          referrerDTO.getUtmTerm(),
          referrerDTO.getLandingPageUrl(),
          referrerDTO.getLastPageUrl(),
          chainJson,
          metadataJson,
          referrerDomain,
          sanitizedUserAgent,
          maskedIp,
          eventTimestamp);

      referrerTrackingRepository.save(tracking);
      recordMetric("content", "updated");
      return true;
    }

    ReferrerTracking tracking =
        ReferrerTracking.forContent(
            contentId,
            referrerDTO.getPageUrl(),
            resolvedReferrerUrl,
            referrerDTO.getUtmSource(),
            referrerDTO.getUtmMedium(),
            referrerDTO.getUtmCampaign(),
            referrerDTO.getUtmContent(),
            referrerDTO.getUtmTerm(),
            referrerDTO.getLandingPageUrl(),
            referrerDTO.getLastPageUrl(),
            chainJson,
            metadataJson,
            referrerDTO.getSessionId(),
            referrerDomain,
            sanitizedUserAgent,
            maskedIp,
            eventTimestamp);

    referrerTrackingRepository.save(tracking);
    recordMetric("content", "stored");
    return true;
  }

  private boolean persistMarketReferrerTracking(
      String marketLinkUrl,
      ReferrerDTO referrerDTO,
      String refererHeader,
      String userAgent,
      String clientIp)
      throws JsonProcessingException {
    if (!StringUtils.hasText(referrerDTO.getSessionId())) {
      log.debug("Skip market tracking due to missing sessionId. marketLinkUrl={}", marketLinkUrl);
      recordMetric("market", "invalid_session");
      return false;
    }

    String resolvedReferrerUrl =
        normalizeReferrerUrl(
            resolveReferrerUrl(referrerDTO, refererHeader), referrerDTO.getPageUrl());
    String chainJson = toReferrerChainJson(referrerDTO.getReferrerChain());
    String metadataJson = toMetadataJson(referrerDTO);
    String maskedIp = maskIpAddress(clientIp);
    String sanitizedUserAgent = sanitizeUserAgent(resolveUserAgent(userAgent, referrerDTO));
    LocalDateTime eventTimestamp = defaultEventTimestamp(referrerDTO.getTimestamp());

    String chainFallbackMarket = lastElement(referrerDTO.getReferrerChain());
    if (!StringUtils.hasText(resolvedReferrerUrl) && StringUtils.hasText(chainFallbackMarket)) {
      resolvedReferrerUrl = normalizeReferrerUrl(chainFallbackMarket, referrerDTO.getPageUrl());
    }

    String firstReferrerUrl = referrerDTO.getFirstReferrerUrl();
    if (!StringUtils.hasText(resolvedReferrerUrl) && StringUtils.hasText(firstReferrerUrl)) {
      resolvedReferrerUrl = normalizeReferrerUrl(firstReferrerUrl, referrerDTO.getPageUrl());
    }

    String referrerDomain = resolveReferrerDomain(resolvedReferrerUrl);
    String lastPageUrl = referrerDTO.getLastPageUrl();
    if ((!StringUtils.hasText(referrerDomain)
            || ReferrerDomainUtils.isInternalDomain(referrerDomain))
        && StringUtils.hasText(lastPageUrl)) {
      resolvedReferrerUrl = normalizeReferrerUrl(lastPageUrl, referrerDTO.getPageUrl());
      referrerDomain = resolveReferrerDomain(resolvedReferrerUrl);
    }

    Optional<ReferrerTracking> existing =
        referrerTrackingRepository.findRecentMarketTracking(
            referrerDTO.getSessionId(), marketLinkUrl);

    if (existing.isPresent()) {
      ReferrerTracking tracking = existing.get();
      if (isDuplicateTracking(
          tracking,
          referrerDTO,
          chainJson,
          metadataJson,
          resolvedReferrerUrl,
          referrerDomain,
          eventTimestamp,
          sanitizedUserAgent,
          maskedIp)) {
        log.debug(
            "Detected duplicate market tracking. marketLinkUrl={}, sessionId={}, pageUrl={}",
            marketLinkUrl,
            referrerDTO.getSessionId(),
            referrerDTO.getPageUrl());
        recordMetric("market", "duplicate");
        return false;
      }

      tracking.refreshTracking(
          referrerDTO.getPageUrl(),
          resolvedReferrerUrl,
          referrerDTO.getUtmSource(),
          referrerDTO.getUtmMedium(),
          referrerDTO.getUtmCampaign(),
          referrerDTO.getUtmContent(),
          referrerDTO.getUtmTerm(),
          referrerDTO.getLandingPageUrl(),
          referrerDTO.getLastPageUrl(),
          chainJson,
          metadataJson,
          referrerDomain,
          sanitizedUserAgent,
          maskedIp,
          eventTimestamp);

      referrerTrackingRepository.save(tracking);
      recordMetric("market", "updated");
      return true;
    }

    ReferrerTracking tracking =
        ReferrerTracking.forMarket(
            marketLinkUrl,
            referrerDTO.getPageUrl(),
            resolvedReferrerUrl,
            referrerDTO.getUtmSource(),
            referrerDTO.getUtmMedium(),
            referrerDTO.getUtmCampaign(),
            referrerDTO.getUtmContent(),
            referrerDTO.getUtmTerm(),
            referrerDTO.getLandingPageUrl(),
            referrerDTO.getLastPageUrl(),
            chainJson,
            metadataJson,
            referrerDTO.getSessionId(),
            referrerDomain,
            sanitizedUserAgent,
            maskedIp,
            eventTimestamp);

    referrerTrackingRepository.save(tracking);
    recordMetric("market", "stored");
    return true;
  }

  private String resolveReferrerUrl(ReferrerDTO referrerDTO) {
    return resolveReferrerUrl(referrerDTO, null);
  }

  private String resolveReferrerUrl(ReferrerDTO referrerDTO, String refererHeader) {
    if (referrerDTO == null) {
      return null;
    }
    String direct = referrerDTO.getReferrerUrl();
    if (StringUtils.hasText(direct)) {
      return direct;
    }
    Map<String, Object> referrerInfo = referrerDTO.getReferrerInfo();
    if (referrerInfo != null && !referrerInfo.isEmpty()) {
      Object infoReferrer = referrerInfo.get("referrerUrl");
      if (infoReferrer instanceof String str && StringUtils.hasText(str)) {
        return str;
      }
      Object fallbackReferrer = referrerInfo.get("url");
      if (fallbackReferrer instanceof String str && StringUtils.hasText(str)) {
        return str;
      }
    }
    Map<String, Object> details = referrerDTO.getReferrerDetails();
    if (details != null && !details.isEmpty()) {
      String[] candidates = {
        "documentReferrer",
        "document_referrer",
        "openerUrl",
        "opener",
        "parentUrl",
        "parent",
        "referrer",
        "referrerUrl",
        "origin",
        "sourceUrl",
        "externalReferrer"
      };
      for (String key : candidates) {
        Object value = details.get(key);
        if (value instanceof String str && StringUtils.hasText(str)) {
          return str;
        }
      }
    }
    if (StringUtils.hasText(refererHeader)) {
      return refererHeader;
    }
    return null;
  }

  private String resolveReferrerDomain(String referrerUrl) {
    if (!StringUtils.hasText(referrerUrl)) {
      return null;
    }

    String host = extractHost(referrerUrl);
    if (!StringUtils.hasText(host)) {
      host = extractHost("http://" + referrerUrl);
    }
    if (!StringUtils.hasText(host)) {
      return null;
    }

    String normalized = host.toLowerCase();
    return normalized.startsWith("www.") ? normalized.substring(4) : normalized;
  }

  private String extractHost(String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (!StringUtils.hasText(host)) {
        return null;
      }
      int port = uri.getPort();
      return port > 0 ? host + ":" + port : host;
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private String toReferrerChainJson(List<String> referrerChain) throws JsonProcessingException {
    if (referrerChain == null || referrerChain.isEmpty()) {
      return "[]";
    }
    return objectMapper.writeValueAsString(referrerChain);
  }

  private String lastElement(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    for (int i = values.size() - 1; i >= 0; i--) {
      String candidate = values.get(i);
      if (StringUtils.hasText(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  private String decodeUrl(String url) {
    if (!StringUtils.hasText(url)) {
      return url;
    }
    try {
      return java.net.URLDecoder.decode(url, java.nio.charset.StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return url;
    }
  }

  private String ensureAbsoluteUrl(String url, String pageUrl) {
    if (!StringUtils.hasText(url)) {
      return url;
    }
    try {
      java.net.URI candidate = new java.net.URI(url);
      if (candidate.isAbsolute()) {
        return candidate.toString();
      }
      if (!StringUtils.hasText(pageUrl)) {
        return url;
      }
      java.net.URI base = new java.net.URI(pageUrl);
      return base.resolve(candidate).toString();
    } catch (java.net.URISyntaxException e) {
      return url;
    }
  }

  private String normalizeReferrerUrl(String url, String pageUrl) {
    if (!StringUtils.hasText(url)) {
      return null;
    }

    String current = decodeUrl(url);

    for (int i = 0; i < 3; i++) {
      String unwrapped = unwrapRedirect(current);
      if (!StringUtils.hasText(unwrapped) || unwrapped.equals(current)) {
        break;
      }
      current = decodeUrl(unwrapped);
    }

    current = ensureAbsoluteUrl(current, pageUrl);
    current = canonicalizeDomainSpecific(current);
    return current;
  }

  private String unwrapRedirect(String url) {
    if (!StringUtils.hasText(url)) {
      return url;
    }
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (!StringUtils.hasText(host) && StringUtils.hasText(uri.getAuthority())) {
        host = uri.getAuthority();
      }
      if (!StringUtils.hasText(host)) {
        return url;
      }
      String lowerHost = host.toLowerCase();
      if ("l.threads.com".equals(lowerHost)) {
        String redirected =
            firstNonBlank(
                getQueryParameter(uri, "u"),
                getQueryParameter(uri, "url"),
                getQueryParameter(uri, "target"),
                getQueryParameter(uri, "redirect"));
        if (StringUtils.hasText(redirected)) {
          return redirected;
        }
        return "https://www.threads.com/";
      }
      return url;
    } catch (URISyntaxException e) {
      return url;
    }
  }

  private String canonicalizeDomainSpecific(String url) {
    if (!StringUtils.hasText(url)) {
      return url;
    }
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (!StringUtils.hasText(host) && StringUtils.hasText(uri.getAuthority())) {
        host = uri.getAuthority();
      }
      if (!StringUtils.hasText(host)) {
        return url;
      }
      String lowerHost = host.toLowerCase();

      if ("blog.naver.com".equals(lowerHost)) {
        String path = uri.getPath();
        if (path != null && path.equalsIgnoreCase("/PostView.naver")) {
          Map<String, String> params = splitQuery(uri.getRawQuery());
          String blogId = firstNonBlank(params.get("blogId"), params.get("blogid"));
          String logNo = firstNonBlank(params.get("logNo"), params.get("logno"));
          if (StringUtils.hasText(blogId) && StringUtils.hasText(logNo)) {
            return "https://blog.naver.com/" + blogId + "/" + logNo;
          }
        }
      }

      return url;
    } catch (URISyntaxException e) {
      return url;
    }
  }

  private String getQueryParameter(URI uri, String... keys) {
    if (uri == null || keys == null || keys.length == 0) {
      return null;
    }
    Map<String, String> params = splitQuery(uri.getRawQuery());
    for (String key : keys) {
      if (!StringUtils.hasText(key)) {
        continue;
      }
      String value = params.get(key);
      if (StringUtils.hasText(value)) {
        return value;
      }
    }
    return null;
  }

  private Map<String, String> splitQuery(String rawQuery) {
    Map<String, String> params = new LinkedHashMap<>();
    if (!StringUtils.hasText(rawQuery)) {
      return params;
    }

    String[] pairs = rawQuery.split("&");
    for (String pair : pairs) {
      if (!StringUtils.hasText(pair)) {
        continue;
      }
      int idx = pair.indexOf('=');
      String key = idx >= 0 ? pair.substring(0, idx) : pair;
      String value = idx >= 0 ? pair.substring(idx + 1) : "";
      key = decodeComponent(key);
      value = decodeComponent(value);
      if (!params.containsKey(key)) {
        params.put(key, value);
      }
    }
    return params;
  }

  private String decodeComponent(String value) {
    if (!StringUtils.hasText(value)) {
      return value;
    }
    try {
      return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return value;
    }
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (StringUtils.hasText(value)) {
        return value;
      }
    }
    return null;
  }

  private String toMetadataJson(ReferrerDTO referrerDTO) throws JsonProcessingException {
    if (referrerDTO == null) {
      return null;
    }

    Map<String, Object> metadata = new LinkedHashMap<>();

    Map<String, Object> referrerDetails = referrerDTO.getReferrerDetails();
    if (referrerDetails != null && !referrerDetails.isEmpty()) {
      metadata.putAll(referrerDetails);
    }

    Map<String, Object> referrerInfo = referrerDTO.getReferrerInfo();
    if (referrerInfo != null && !referrerInfo.isEmpty()) {
      metadata.put("referrerInfo", referrerInfo);
    }

    putIfHasText(metadata, "connectionType", referrerDTO.getConnectionType());
    putIfNotNull(metadata, "deviceMemory", referrerDTO.getDeviceMemory());
    putIfNotNull(metadata, "hardwareConcurrency", referrerDTO.getHardwareConcurrency());
    putIfHasText(metadata, "language", referrerDTO.getLanguage());
    putIfHasText(metadata, "platform", referrerDTO.getPlatform());
    putIfHasText(metadata, "screenResolution", referrerDTO.getScreenResolution());
    putIfHasText(metadata, "timezone", referrerDTO.getTimezone());

    String reportedUserAgent = referrerDTO.getUserAgent();
    if (StringUtils.hasText(reportedUserAgent)) {
      metadata.put("reportedUserAgent", reportedUserAgent);
    }

    Map<String, Object> socialAppInfo = referrerDTO.getSocialAppInfo();
    if (socialAppInfo != null && !socialAppInfo.isEmpty()) {
      metadata.put("socialAppInfo", socialAppInfo);
    }
    if ((socialAppInfo == null || socialAppInfo.isEmpty()) && referrerInfo != null) {
      Object nestedSocial = referrerInfo.get("socialAppInfo");
      if (nestedSocial instanceof Map<?, ?>) {
        Map<?, ?> nestedMap = (Map<?, ?>) nestedSocial;
        if (!nestedMap.isEmpty()) {
          metadata.put("socialAppInfo", nestedMap);
        }
      }
    }

    Map<String, Object> clientHints = referrerDTO.getClientHints();
    if (clientHints != null && !clientHints.isEmpty()) {
      metadata.put("clientHints", clientHints);
    }

    putIfHasText(metadata, "firstLandingPageUrl", referrerDTO.getFirstLandingPageUrl());
    putIfHasText(metadata, "firstReferrerUrl", referrerDTO.getFirstReferrerUrl());

    if (metadata.isEmpty()) {
      return null;
    }

    return objectMapper.writeValueAsString(metadata);
  }

  private LocalDateTime defaultEventTimestamp(LocalDateTime timestamp) {
    return timestamp != null ? timestamp : LocalDateTime.now(ASIA_SEOUL);
  }

  private void putIfHasText(Map<String, Object> target, String key, String value) {
    if (target == null || !StringUtils.hasText(key)) {
      return;
    }
    if (StringUtils.hasText(value)) {
      target.put(key, value);
    }
  }

  private void putIfNotNull(Map<String, Object> target, String key, Object value) {
    if (target == null || !StringUtils.hasText(key)) {
      return;
    }
    if (value != null) {
      target.put(key, value);
    }
  }

  private String resolveUserAgent(String headerUserAgent, ReferrerDTO referrerDTO) {
    if (StringUtils.hasText(headerUserAgent)) {
      return headerUserAgent;
    }
    if (referrerDTO != null && StringUtils.hasText(referrerDTO.getUserAgent())) {
      return referrerDTO.getUserAgent();
    }
    return null;
  }

  private boolean isDuplicateTracking(
      ReferrerTracking existing,
      ReferrerDTO incoming,
      String incomingChain,
      String incomingMetadata,
      String incomingReferrerUrl,
      String incomingReferrerDomain,
      LocalDateTime incomingTimestamp,
      String sanitizedUserAgent,
      String maskedIpAddress) {

    boolean matchesCore =
        equalsNullable(existing.getPageUrl(), incoming.getPageUrl())
            && equalsNullable(existing.getReferrerUrl(), incomingReferrerUrl)
            && equalsNullable(existing.getReferrerDomain(), incomingReferrerDomain)
            && equalsNullable(existing.getLandingPageUrl(), incoming.getLandingPageUrl())
            && equalsNullable(existing.getLastPageUrl(), incoming.getLastPageUrl())
            && equalsNullable(existing.getReferrerChain(), incomingChain)
            && equalsNullable(existing.getReferrerMetadata(), incomingMetadata)
            && equalsNullable(existing.getSessionId(), incoming.getSessionId())
            && equalsNullable(existing.getUserAgent(), sanitizedUserAgent)
            && equalsNullable(existing.getIpAddress(), maskedIpAddress);

    if (!matchesCore) {
      return false;
    }

    LocalDateTime existingTimestamp =
        existing.getEventTimestamp() != null
            ? existing.getEventTimestamp()
            : existing.getCreatedAt();

    if (existingTimestamp == null || incomingTimestamp == null) {
      return false;
    }

    long minutesBetween =
        Math.abs(Duration.between(existingTimestamp, incomingTimestamp).toMinutes());
    return minutesBetween < 5;
  }

  private String maskIpAddress(String ipAddress) {
    if (!StringUtils.hasText(ipAddress)) {
      return null;
    }

    if (ipAddress.contains(".")) {
      String[] parts = ipAddress.split("\\.");
      if (parts.length == 4) {
        parts[3] = "0";
        return String.join(".", parts);
      }
      return ipAddress;
    }

    if (ipAddress.contains(":")) {
      String[] parts = ipAddress.split(":");
      int start = Math.max(0, parts.length - 4);
      for (int i = start; i < parts.length; i++) {
        parts[i] = "****";
      }
      return String.join(":", parts);
    }

    return ipAddress;
  }

  private String sanitizeUserAgent(String userAgent) {
    if (!StringUtils.hasText(userAgent)) {
      return null;
    }
    return userAgent.length() > 1000 ? userAgent.substring(0, 1000) : userAgent;
  }

  private boolean equalsNullable(String left, String right) {
    if (left == null && right == null) {
      return true;
    }
    if (left == null || right == null) {
      return false;
    }
    return left.equals(right);
  }

  private void recordMetric(String type, String outcome) {
    meterRegistry.counter(METRIC_NAME, "type", type, "outcome", outcome).increment();
  }

  private ContentReferrerStats findOrCreateContentReferrerStats(
      Long contentId, ReferrerDTO referrerDTO) {
    log.info("--- findOrCreateContentReferrerStats START ---");

    String resolvedReferrerUrl =
        normalizeReferrerUrl(resolveReferrerUrl(referrerDTO), referrerDTO.getPageUrl());

    // referrerUrl에서 도메인 추출
    String referrerDomain = extractDomainFromUrl(resolvedReferrerUrl);
    log.info(
        "Extracted referrerDomain: {} from referrerUrl: {}", referrerDomain, resolvedReferrerUrl);

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
            .referrerUrl(resolvedReferrerUrl)
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

    String resolvedReferrerUrl =
        normalizeReferrerUrl(resolveReferrerUrl(referrerDTO), referrerDTO.getPageUrl());

    // referrerUrl에서 도메인 추출
    String referrerDomain = extractDomainFromUrl(resolvedReferrerUrl);
    log.info(
        "Extracted referrerDomain: {} from referrerUrl: {}", referrerDomain, resolvedReferrerUrl);

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
            .referrerUrl(resolvedReferrerUrl)
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
