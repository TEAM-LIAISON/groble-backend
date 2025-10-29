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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
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

/**
 *
 *
 * <h1>유입 경로 집계 서비스</h1>
 *
 * <p>본 서비스는 다음과 같은 규칙에 따라 유입 경로를 집계합니다.
 *
 * <h2>1. 직접 유입 (Direct)</h2>
 *
 * <ul>
 *   <li><strong>정의:</strong> 외부 유입 경로 정보가 없는 모든 경우.
 *   <li><strong>사례:</strong>
 *       <ul>
 *         <li>브라우저 주소창에 URL을 직접 입력하여 접속
 *         <li>북마크(즐겨찾기)를 통해 접속
 *         <li>브라우저가 리퍼러(이전 페이지) 정보를 제공하지 않는 모든 경우
 *       </ul>
 *   <li><strong>처리:</strong> 유입 소스는 <code>(direct)</code>로 기록됩니다.
 * </ul>
 *
 * <h2>2. 외부 유입 (External)</h2>
 *
 * <ul>
 *   <li><strong>정의:</strong> <code>google.com</code>, <code>instagram.com</code> 등 서비스 외부 도메인에서 유입된
 *       모든 경우.
 *   <li><strong>처리:</strong> 정상적으로 유입 경로로 집계되며, 어떤 도메인에서 유입되었는지 기록됩니다.
 * </ul>
 *
 * <h2>3. 내부 이동 (Internal Navigation) - UTM 파라미터가 없는 경우</h2>
 *
 * <ul>
 *   <li><strong>기본 원칙:</strong> 단순 페이지 이동은 새로운 유입으로 집계하지 않습니다.
 *   <li><strong>유일한 예외:</strong> 사용자의 핵심적인 이동 경로인 <strong>마켓 페이지 → 콘텐츠 페이지</strong>로의 이동만 새로운 유입으로
 *       집계합니다.
 *   <li><strong>집계되지 않는 이동의 예:</strong>
 *       <ul>
 *         <li>콘텐츠 → 마켓 (뒤로가기 등)
 *         <li>콘텐츠 → 콘텐츠
 *         <li>마켓 → 마켓
 *         <li>로그인 페이지 → 마켓/콘텐츠 페이지 등
 *       </ul>
 * </ul>
 *
 * <h2>4. 내부 캠페인 (Internal Campaign)</h2>
 *
 * <ul>
 *   <li><strong>정의:</strong> 내부 이동이라도 URL에 UTM 파라미터(<code>utm_source</code> 등)가 포함된 경우.
 *   <li><strong>처리:</strong> 내부 마케팅 활동 추적을 위해, 예외적으로 새로운 유입으로 집계합니다.
 * </ul>
 *
 * <h2>5. 자가 유입 (Self-view)</h2>
 *
 * <ul>
 *   <li><strong>정의:</strong> 로그인한 사용자가 자신이 소유한 마켓 또는 콘텐츠를 방문하는 경우.
 *   <li><strong>처리:</strong> 유입 통계에 영향을 주지 않도록, 집계에서 완전히 제외됩니다.
 * </ul>
 *
 * <h2>6. 중복 집계 방지</h2>
 *
 * <ul>
 *   <li><strong>정의:</strong> 5분 이내에 동일한 세션, 동일한 리퍼러, 동일한 UTM 값을 가진 요청이 다시 들어온 경우.
 *   <li><strong>처리:</strong> 페이지 새로고침 등으로 인한 노이즈로 간주하여 중복으로 집계하지 않습니다.
 * </ul>
 */
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
  private final ContentReader contentReader;
  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;
  private final ConcurrentMap<String, Boolean> marketLinkCache = new ConcurrentHashMap<>();

  public void recordContentReferrer(
      Long contentId,
      ReferrerDTO referrerDTO,
      String refererHeader,
      String userAgent,
      String clientIp,
      Long userId) {
    if (referrerDTO == null) {
      log.warn("Received null ReferrerDTO for contentId={}.", contentId);
      return;
    }

    // 5. 자가 유입 (Self-view)
    if (userId != null) {
      try {
        Content content = contentReader.getContentById(contentId);
        if (content.getUser() != null && userId.equals(content.getUser().getId())) {
          log.debug(
              "Skipping content referrer tracking for own content. contentId={}, userId={}",
              contentId,
              userId);
          recordMetric("content", "self_view");
          return;
        }
      } catch (Exception e) {
        log.warn(
            "Could not verify content ownership for self-view check. contentId={}, userId={}",
            contentId,
            userId,
            e);
      }
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
      String clientIp,
      Long userId) {
    if (referrerDTO == null) {
      log.warn("Received null ReferrerDTO for marketLinkUrl={}.", marketLinkUrl);
      return;
    }

    Market market;
    try {
      market = userReader.getMarketWithUser(marketLinkUrl);
    } catch (Exception e) {
      log.error("Failed to find market for referrer tracking: {}", marketLinkUrl, e);
      return;
    }

    // 5. 자가 유입 (Self-view)
    if (userId != null && market.getUser() != null && userId.equals(market.getUser().getId())) {
      log.debug(
          "Skipping market referrer tracking for own market. marketLinkUrl={}, userId={}",
          marketLinkUrl,
          userId);
      recordMetric("market", "self_view");
      return;
    }

    boolean persisted;
    try {
      persisted =
          persistMarketReferrerTracking(
              marketLinkUrl, referrerDTO, refererHeader, userAgent, clientIp);
    } catch (Exception e) {
      log.error(
          "Failed to persist market referrer tracking for marketLinkUrl={}", marketLinkUrl, e);
      recordMetric("market", "error");
      return;
    }

    if (!persisted) {
      return;
    }

    try {
      MarketReferrerStats stats = findOrCreateMarketReferrerStats(market.getId(), referrerDTO);
      stats.incrementVisitCount();
      marketReferrerStatsRepository.save(stats);

      MarketReferrerEvent event =
          MarketReferrerEvent.builder()
              .referrerStatsId(stats.getId())
              .marketId(market.getId())
              .eventDate(LocalDateTime.now())
              .build();

      marketReferrerEventRepository.save(event);

    } catch (Exception e) {
      log.error("Failed to record market referrer stats for marketLinkUrl: " + marketLinkUrl, e);
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

    String sessionId = referrerDTO.getSessionId();

    if (!StringUtils.hasText(sessionId)) {

      log.debug("Skip content tracking due to missing sessionId. contentId={}", contentId);

      recordMetric("content", "invalid_session");

      return false;
    }

    LocalDateTime eventTimestamp = defaultEventTimestamp(referrerDTO.getTimestamp());

    String resolvedReferrerUrl =
        normalizeReferrerUrl(
            resolveReferrerUrl(referrerDTO, refererHeader), referrerDTO.getPageUrl());

    // 6. 중복 집계 방지

    Optional<ReferrerTracking> existing =
        referrerTrackingRepository.findRecentContentTracking(sessionId, contentId.toString());

    if (existing.isPresent()
        && isDuplicateTracking(existing.get(), referrerDTO, resolvedReferrerUrl, eventTimestamp)) {

      log.debug(
          "Detected duplicate content tracking. contentId={}, sessionId={}", contentId, sessionId);

      recordMetric("content", "duplicate");

      return false;
    }

    String referrerDomain = resolveReferrerDomain(resolvedReferrerUrl);

    // 홈페이지에서 온 경우 명시적으로 처리

    if (Boolean.TRUE.equals(referrerDTO.isFromHomepage())) {

      resolvedReferrerUrl = "https://groble.im";

      referrerDomain = "groble.im";

      log.debug("Recording homepage referral for contentId={}", contentId);

    } else {

      // 1. 직접 유입이 아닌 경우에만 내부/외부 유입 규칙 적용

      if (StringUtils.hasText(resolvedReferrerUrl)) {

        boolean isInternalCampaign = hasUtmParameters(referrerDTO);

        // 3. 내부 이동 & 4. 내부 캠페인 (UTM 파라미터가 없는 경우)

        if (ReferrerDomainUtils.isInternalDomain(referrerDomain) && !isInternalCampaign) {

          boolean fromHome = false;

          try {

            URI referrerUri = new URI(resolvedReferrerUrl);

            String path = referrerUri.getPath();

            if (path == null || path.equals("/") || path.isEmpty()) {

              fromHome = true;
            }

          } catch (URISyntaxException e) {

            log.warn("Could not parse referrer URI: {}", resolvedReferrerUrl, e);
          }

          // 기본 경로("/")에서 온 경우 직접 유입처럼 처리

          if (fromHome) {

            resolvedReferrerUrl = null;

            referrerDomain = null;

          } else {

            // 그 외 모든 내부 이동은 차단

            log.debug("Skipping internal navigation for contentId={}", contentId);

            recordMetric("content", "internal_navigation_skipped");

            return false;
          }
        }

        // 2. 외부 유입 또는 내부 캠페인이면 항상 통과

      }
    }

    // admin.groble.im 유입은 항상 제외

    if (StringUtils.hasText(referrerDomain) && referrerDomain.contains("admin.groble.im")) {

      log.debug("Skip admin referral for contentId={}.", contentId);

      recordMetric("content", "ignored_admin");

      return false;
    }

    // 최종적으로 리퍼러가 없으면 (direct)로 설정

    if (!StringUtils.hasText(resolvedReferrerUrl)) {

      referrerDomain = "(direct)";
    }

    String chainJson = toReferrerChainJson(referrerDTO.getReferrerChain());

    String metadataJson = toMetadataJson(referrerDTO);

    String maskedIp = maskIpAddress(clientIp);

    String sanitizedUserAgent = sanitizeUserAgent(resolveUserAgent(userAgent, referrerDTO));

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
            sessionId,
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

    String sessionId = referrerDTO.getSessionId();

    if (!StringUtils.hasText(sessionId)) {

      log.debug("Skip market tracking due to missing sessionId. marketLinkUrl={}", marketLinkUrl);

      recordMetric("market", "invalid_session");

      return false;
    }

    LocalDateTime eventTimestamp = defaultEventTimestamp(referrerDTO.getTimestamp());

    String resolvedReferrerUrl =
        normalizeReferrerUrl(
            resolveReferrerUrl(referrerDTO, refererHeader), referrerDTO.getPageUrl());

    // 6. 중복 집계 방지

    Optional<ReferrerTracking> existing =
        referrerTrackingRepository.findRecentMarketTracking(sessionId, marketLinkUrl);

    if (existing.isPresent()
        && isDuplicateTracking(existing.get(), referrerDTO, resolvedReferrerUrl, eventTimestamp)) {

      log.debug(
          "Detected duplicate market tracking. marketLinkUrl={}, sessionId={}",
          marketLinkUrl,
          sessionId);

      recordMetric("market", "duplicate");

      return false;
    }

    String referrerDomain = resolveReferrerDomain(resolvedReferrerUrl);

    // 홈페이지에서 온 경우 명시적으로 처리

    if (Boolean.TRUE.equals(referrerDTO.isFromHomepage())) {

      resolvedReferrerUrl = "https://groble.im";

      referrerDomain = "groble.im";

      log.debug("Recording homepage referral for marketLinkUrl={}", marketLinkUrl);

    } else {

      // 1. 직접 유입이 아닌 경우에만 내부/외부 유입 규칙 적용

      if (StringUtils.hasText(resolvedReferrerUrl)) {

        boolean isInternalCampaign = hasUtmParameters(referrerDTO);

        // 3. 내부 이동 & 4. 내부 캠페인

        if (ReferrerDomainUtils.isInternalDomain(referrerDomain) && !isInternalCampaign) {

          try {

            URI referrerUri = new URI(resolvedReferrerUrl);

            String path = referrerUri.getPath();

            // 기본 경로("/")에서 온 경우 직접 유입처럼 처리

            if (path == null || path.equals("/") || path.isEmpty()) {

              resolvedReferrerUrl = null;

              referrerDomain = null;

            } else {

              log.debug("Skipping internal navigation for marketLinkUrl={}", marketLinkUrl);

              recordMetric("market", "internal_navigation_skipped");

              return false;
            }

          } catch (URISyntaxException e) {

            log.warn(
                "Could not parse referrer URI for internal navigation check: {}",
                resolvedReferrerUrl,
                e);

            log.debug(
                "Skipping internal navigation due to URI parse error for marketLinkUrl={}",
                marketLinkUrl);

            recordMetric("market", "internal_navigation_skipped");

            return false;
          }
        }

        // 외부 유입 또는 내부 캠페인이면 통과

      }
    }

    // admin.groble.im 유입은 항상 제외

    if (StringUtils.hasText(referrerDomain) && referrerDomain.contains("admin.groble.im")) {

      log.debug("Skip admin referral for marketLinkUrl={}.", marketLinkUrl);

      recordMetric("market", "ignored_admin");

      return false;
    }

    // 최종적으로 리퍼러가 없으면 (direct)로 설정

    if (!StringUtils.hasText(resolvedReferrerUrl)) {

      referrerDomain = "(direct)";
    }

    String chainJson = toReferrerChainJson(referrerDTO.getReferrerChain());

    String metadataJson = toMetadataJson(referrerDTO);

    String maskedIp = maskIpAddress(clientIp);

    String sanitizedUserAgent = sanitizeUserAgent(resolveUserAgent(userAgent, referrerDTO));

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
            sessionId,
            referrerDomain,
            sanitizedUserAgent,
            maskedIp,
            eventTimestamp);

    referrerTrackingRepository.save(tracking);

    recordMetric("market", "stored");

    return true;
  }

  private boolean hasUtmParameters(ReferrerDTO referrerDTO) {
    return StringUtils.hasText(referrerDTO.getUtmSource())
        || StringUtils.hasText(referrerDTO.getUtmMedium())
        || StringUtils.hasText(referrerDTO.getUtmCampaign());
  }

  private String resolveReferrerUrl(ReferrerDTO referrerDTO, String refererHeader) {
    if (referrerDTO == null) {
      return refererHeader;
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

      if (ReferrerDomainUtils.isInternalDomain(lowerHost)) {
        return canonicalizeGrobleInternalUrl(uri);
      }

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

  private String canonicalizeGrobleInternalUrl(URI uri) {
    String baseUrl = "https://groble.im";
    String marketCandidate = firstPathSegment(uri.getPath());
    if (isKnownMarketLink(marketCandidate)) {
      return baseUrl + "/" + marketCandidate;
    }
    return baseUrl;
  }

  private String firstPathSegment(String path) {
    if (!StringUtils.hasText(path)) {
      return null;
    }
    String[] segments = path.split("/");
    for (String segment : segments) {
      if (StringUtils.hasText(segment)) {
        return segment;
      }
    }
    return null;
  }

  private boolean isKnownMarketLink(String candidate) {
    if (!StringUtils.hasText(candidate)) {
      return false;
    }
    Boolean cached = marketLinkCache.get(candidate);
    if (Boolean.TRUE.equals(cached)) {
      return true;
    }
    boolean exists = userReader.existsByMarketLinkUrl(candidate);
    if (exists) {
      marketLinkCache.put(candidate, Boolean.TRUE);
    } else {
      marketLinkCache.remove(candidate);
    }
    return exists;
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
      if (nestedSocial instanceof Map) {
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
      String incomingReferrerUrl,
      LocalDateTime incomingTimestamp) {

    boolean matchesCore =
        equalsNullable(existing.getSessionId(), incoming.getSessionId())
            && equalsNullable(existing.getReferrerUrl(), incomingReferrerUrl)
            && equalsNullable(existing.getUtmSource(), incoming.getUtmSource())
            && equalsNullable(existing.getUtmMedium(), incoming.getUtmMedium())
            && equalsNullable(existing.getUtmCampaign(), incoming.getUtmCampaign());

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
    String resolvedReferrerUrl =
        normalizeReferrerUrl(resolveReferrerUrl(referrerDTO, null), referrerDTO.getPageUrl());
    String referrerDomain = extractDomainFromUrl(resolvedReferrerUrl);
    String source =
        referrerDTO.getUtmSource() != null
            ? referrerDTO.getUtmSource()
            : mapDomainToSource(referrerDomain);
    String medium =
        referrerDTO.getUtmMedium() != null
            ? referrerDTO.getUtmMedium()
            : inferMediumFromDomain(referrerDomain);
    String campaign = referrerDTO.getUtmCampaign();

    List<ContentReferrerStats> existingList =
        contentReferrerStatsRepository
            .findAllByContentIdAndReferrerDomainAndSourceAndMediumAndCampaign(
                contentId, referrerDomain, source, medium, campaign);

    if (!existingList.isEmpty()) {
      if (existingList.size() > 1) {
        ContentReferrerStats primary = existingList.get(0);
        for (int i = 1; i < existingList.size(); i++) {
          ContentReferrerStats duplicate = existingList.get(i);
          primary.consolidateVisitCount(duplicate.getVisitCount());
          contentReferrerStatsRepository.delete(duplicate);
        }
        return contentReferrerStatsRepository.save(primary);
      }
      return existingList.get(0);
    }

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
            .visitCount(0) // incrementVisitCount에서 1로 시작
            .build();
    stats.parseReferrerUrl();
    return contentReferrerStatsRepository.save(stats);
  }

  private MarketReferrerStats findOrCreateMarketReferrerStats(
      Long marketId, ReferrerDTO referrerDTO) {
    String resolvedReferrerUrl =
        normalizeReferrerUrl(resolveReferrerUrl(referrerDTO, null), referrerDTO.getPageUrl());
    String referrerDomain = extractDomainFromUrl(resolvedReferrerUrl);
    String source =
        referrerDTO.getUtmSource() != null
            ? referrerDTO.getUtmSource()
            : mapDomainToSource(referrerDomain);
    String medium =
        referrerDTO.getUtmMedium() != null
            ? referrerDTO.getUtmMedium()
            : inferMediumFromDomain(referrerDomain);
    String campaign = referrerDTO.getUtmCampaign();

    List<MarketReferrerStats> existingList =
        marketReferrerStatsRepository
            .findAllByMarketIdAndReferrerDomainAndSourceAndMediumAndCampaign(
                marketId, referrerDomain, source, medium, campaign);

    if (!existingList.isEmpty()) {
      if (existingList.size() > 1) {
        MarketReferrerStats primary = existingList.get(0);
        for (int i = 1; i < existingList.size(); i++) {
          MarketReferrerStats duplicate = existingList.get(i);
          primary.consolidateVisitCount(duplicate.getVisitCount());
          marketReferrerStatsRepository.delete(duplicate);
        }
        return marketReferrerStatsRepository.save(primary);
      }
      return existingList.get(0);
    }

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
            .visitCount(0) // incrementVisitCount에서 1로 시작
            .build();
    stats.parseReferrerUrl();
    return marketReferrerStatsRepository.save(stats);
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
