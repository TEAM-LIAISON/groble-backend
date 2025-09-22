package liaison.groble.common.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 토큰 쿠키를 추가·삭제하는 서비스 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TokenCookieService {

  private final Environment env;

  @Value("${app.cookie.domain:}") // 기본값을 빈 문자열로 설정
  private String cookieDomain;

  @Value("${app.cookie.admin-domain:}") // 기본값을 빈 문자열로 설정
  private String adminCookieDomain;

  @Value("${app.cookie.domain.dev:}")
  private String devCookieDomain;

  @Value("${app.cookie.admin-domain.dev:}")
  private String adminDevCookieDomain;

  private static final int ACCESS_TOKEN_MAX_AGE = (int) Duration.ofHours(1).toSeconds();
  private static final int REFRESH_TOKEN_MAX_AGE = (int) Duration.ofDays(7).toSeconds();
  private static final int GUEST_TOKEN_MAX_AGE = (int) Duration.ofMinutes(30).toSeconds();
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
  private static final String GUEST_TOKEN_COOKIE_NAME = "guestToken";

  // --- Admin 쿠키 추가 ---
  public void addAdminTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken) {
    clearAdminTokenCookies(request, response);
    HttpServletRequest req = resolveRequest(request);
    CookieSettings settings = resolveAdminSettings(req);

    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    log.info(
        "[Admin] 쿠키 설정: env={}, domain={}, secure={}, sameSite={}, fromLocalhost={}",
        String.join(",", env.getActiveProfiles()),
        settings.domain() != null ? settings.domain() : "(host-only)",
        settings.secure(),
        settings.sameSite(),
        settings.fromLocalhost());
  }

  // --- User 쿠키 추가 ---
  public void addTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken) {
    clearTokenCookies(request, response);

    CookieSettings settings = resolveUserSettings(request);

    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    log.info(
        "[User] 쿠키 설정: env={}, domain={}, secure={}, sameSite={}, fromLocalhost={}",
        String.join(",", env.getActiveProfiles()),
        settings.domain() != null ? settings.domain() : "(host-only)",
        settings.secure(),
        settings.sameSite(),
        settings.fromLocalhost());
  }

  public void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {
    addTokenCookies(null, response, accessToken, refreshToken);
  }

  // --- Guest 쿠키 추가 ---
  public void addGuestTokenCookie(HttpServletResponse response, String guestToken) {
    addGuestTokenCookie(null, response, guestToken);
  }

  public void addGuestTokenCookie(
      HttpServletRequest request, HttpServletResponse response, String guestToken) {
    CookieSettings settings = resolveUserSettings(request);

    CookieUtils.addCookie(
        response,
        GUEST_TOKEN_COOKIE_NAME,
        guestToken,
        GUEST_TOKEN_MAX_AGE,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    log.info(
        "[Guest] 쿠키 설정: env={}, domain={}, secure={}, sameSite={}, fromLocalhost={}",
        String.join(",", env.getActiveProfiles()),
        settings.domain() != null ? settings.domain() : "(host-only)",
        settings.secure(),
        settings.sameSite(),
        settings.fromLocalhost());
  }

  // --- Admin 쿠키 제거 ---
  public void clearAdminTokenCookies(HttpServletRequest request, HttpServletResponse response) {
    HttpServletRequest req = resolveRequest(request);
    CookieSettings settings = resolveAdminSettings(req);

    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        null,
        0,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        null,
        0,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    log.debug(
        "[Admin] 쿠키 제거: domain={}, fromLocalhost={}",
        settings.domain() != null ? settings.domain() : "(host-only)",
        settings.fromLocalhost());
  }

  public void clearAdminTokenCookies(HttpServletResponse response) {
    clearAdminTokenCookies(null, response);
  }

  // --- User 쿠키 제거 ---
  public void clearTokenCookies(HttpServletRequest request, HttpServletResponse response) {
    CookieSettings settings = resolveUserSettings(request);

    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        null,
        0,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        null,
        0,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    log.debug(
        "[User] 쿠키 제거: domain={}, fromLocalhost={}",
        settings.domain() != null ? settings.domain() : "(host-only)",
        settings.fromLocalhost());
  }

  public void clearTokenCookies(HttpServletResponse response) {
    clearTokenCookies(null, response);
  }

  /** 로그아웃 시 쿠키 완전 제거 (Admin) */
  public void removeAdminTokenCookies(HttpServletResponse response) {
    clearAdminTokenCookies(null, response);
    log.info("[Admin] 토큰 쿠키 완전 제거 완료");
  }

  /** 로그아웃 시 쿠키 완전 제거 (User) */
  public void removeTokenCookies(HttpServletResponse response) {
    clearTokenCookies(null, response);
    log.info("[User] 토큰 쿠키 완전 제거 완료");
  }

  // --- Guest 쿠키 제거 ---
  public void clearGuestTokenCookie(HttpServletRequest request, HttpServletResponse response) {
    CookieSettings settings = resolveUserSettings(request);

    CookieUtils.addCookie(
        response,
        GUEST_TOKEN_COOKIE_NAME,
        null,
        0,
        "/",
        true,
        settings.secure(),
        settings.sameSite(),
        settings.domain());

    log.debug(
        "[Guest] 쿠키 제거: domain={}, fromLocalhost={}",
        settings.domain() != null ? settings.domain() : "(host-only)",
        settings.fromLocalhost());
  }

  public void clearGuestTokenCookie(HttpServletResponse response) {
    clearGuestTokenCookie(null, response);
  }

  /** 로그아웃 시 게스트 토큰 쿠키 완전 제거 */
  public void removeGuestTokenCookie(HttpServletResponse response) {
    clearGuestTokenCookie(null, response);
    log.info("[Guest] 토큰 쿠키 완전 제거 완료");
  }

  /** 게스트 토큰 쿠키에서 토큰 값 추출 */
  public String getGuestTokenFromCookie(HttpServletRequest request) {
    if (request == null) return null;
    return CookieUtils.getCookieValue(request, GUEST_TOKEN_COOKIE_NAME);
  }

  // --- 공통 헬퍼: 현재 요청 가져오기 ---
  private HttpServletRequest resolveRequest(HttpServletRequest request) {
    if (request != null) return request;
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs != null ? attrs.getRequest() : null;
  }

  /** 요청이 localhost에서 왔는지 확인 */
  private boolean isRequestFromLocalhost(HttpServletRequest request) {
    if (request == null) return false;
    String origin = request.getHeader("Origin");
    String referer = request.getHeader("Referer");
    String target = origin != null ? origin : referer;
    return target != null && (target.contains("localhost") || target.contains("127.0.0.1"));
  }

  // --- 공통: User 쿠키 설정 계산 ---
  private CookieSettings resolveUserSettings(HttpServletRequest request) {
    HttpServletRequest req = resolveRequest(request);
    boolean fromLocalhost = isRequestFromLocalhost(req);
    boolean isLocalProfile = env.matchesProfiles("local");

    String sameSite;
    boolean secure;
    String domain;

    if (isLocalProfile) {
      sameSite = "Lax";
      secure = false;
      domain = null;
    } else {
      sameSite = "None";
      secure = true;
      domain = determineDomain(req, cookieDomain, devCookieDomain);
    }

    return new CookieSettings(sameSite, secure, domain, fromLocalhost);
  }

  // --- 공통: Admin 쿠키 설정 계산 ---
  private CookieSettings resolveAdminSettings(HttpServletRequest request) {
    HttpServletRequest req = resolveRequest(request);
    boolean fromLocalhost = isRequestFromLocalhost(req);
    boolean isLocalProfile = env.matchesProfiles("local");
    boolean devRequest = isDevRequest(req);

    String sameSite;
    boolean secure;
    String domain = null;

    if (isLocalProfile) {
      sameSite = "Lax";
      secure = false;
    } else {
      secure = true;
      domain = determineDomain(req, adminCookieDomain, adminDevCookieDomain);
      if (fromLocalhost) {
        sameSite = "None";
      } else if (devRequest) {
        sameSite = "Lax";
      } else {
        sameSite = "None";
      }
    }

    return new CookieSettings(sameSite, secure, domain, fromLocalhost);
  }

  private String determineDomain(
      HttpServletRequest request, String primaryDomain, String devDomain) {
    if (request == null) {
      return firstNonBlank(primaryDomain, devDomain);
    }

    String host = request.getServerName();
    if (host == null || host.isBlank()) {
      return firstNonBlank(primaryDomain, devDomain);
    }

    if (isLocalHost(host)) {
      return null;
    }

    String lower = host.toLowerCase(Locale.ROOT);
    boolean devHost = isDevHost(lower);

    if (devHost) {
      return firstNonBlank(devDomain, deriveDomain(lower, 3));
    }

    return firstNonBlank(primaryDomain, deriveDomain(lower, 2));
  }

  private String deriveDomain(String host, int preferredParts) {
    String[] parts = host.split("\\.");
    if (parts.length <= preferredParts) {
      return host;
    }
    int startIdx = parts.length - preferredParts;
    return String.join(".", Arrays.copyOfRange(parts, startIdx, parts.length));
  }

  private boolean isDevRequest(HttpServletRequest request) {
    if (request == null) {
      return env.matchesProfiles("dev");
    }
    String host = request.getServerName();
    if (host == null) {
      return env.matchesProfiles("dev");
    }
    return isDevHost(host.toLowerCase(Locale.ROOT));
  }

  private boolean isDevHost(String lowerHost) {
    return lowerHost.contains(".dev.")
        || lowerHost.startsWith("dev.")
        || lowerHost.endsWith(".dev");
  }

  private boolean isLocalHost(String host) {
    String lower = host.toLowerCase(Locale.ROOT);
    return lower.equals("localhost") || lower.equals("127.0.0.1") || lower.equals("::1");
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  // 설정 값 묶음
  private record CookieSettings(
      String sameSite, boolean secure, String domain, boolean fromLocalhost) {}
}
