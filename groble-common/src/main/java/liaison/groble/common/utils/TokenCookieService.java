package liaison.groble.common.utils;

import java.time.Duration;

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

  @Value("${app.cookie.domain}")
  private String cookieDomain;

  @Value("${app.cookie.admin-domain}")
  private String adminCookieDomain;

  private static final int ACCESS_TOKEN_MAX_AGE = (int) Duration.ofHours(1).toSeconds();
  private static final int REFRESH_TOKEN_MAX_AGE = (int) Duration.ofDays(7).toSeconds();
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

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

  public void addAdminTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {
    addAdminTokenCookies(null, response, accessToken, refreshToken);
  }

  // --- User 쿠키 추가 ---
  public void addTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken) {
    clearTokenCookies(request, response);

    // 요청 출처 확인
    boolean fromLocalhost = false;
    if (request != null) {
      String origin = request.getHeader("Origin");
      String referer = request.getHeader("Referer");
      String target = origin != null ? origin : referer;
      fromLocalhost =
          target != null && (target.contains("localhost") || target.contains("127.0.0.1"));
    }

    boolean isLocal = env.matchesProfiles("local");

    // 쿠키 설정 결정
    String sameSite;
    boolean secure;
    String domain;

    if (fromLocalhost) {
      // localhost에서 온 요청: 크로스 도메인 쿠키 설정
      sameSite = "None";
      secure = true; // SameSite=None은 Secure 필수
      domain = null; // localhost는 도메인 쿠키 지원 안함
    } else if (isLocal) {
      // 로컬 환경 실행
      sameSite = "Lax";
      secure = false;
      domain = null;
    } else {
      // 개발/운영 환경
      sameSite = "None"; // 크로스 도메인 지원
      secure = true;
      domain =
          cookieDomain != null && !cookieDomain.isBlank()
              ? (cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain)
              : null;
    }

    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        secure,
        sameSite,
        domain);

    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        secure,
        sameSite,
        domain);

    log.info(
        "[User] 쿠키 설정: env={}, domain={}, secure={}, sameSite={}, fromLocalhost={}",
        String.join(",", env.getActiveProfiles()),
        domain != null ? domain : "(host-only)",
        secure,
        sameSite,
        fromLocalhost);
  }

  public void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {
    addTokenCookies(null, response, accessToken, refreshToken);
  }

  // --- Admin 쿠키 제거 ---
  public void clearAdminTokenCookies(HttpServletRequest request, HttpServletResponse response) {
    HttpServletRequest req = resolveRequest(request);
    CookieSettings settings = resolveAdminSettings(req);

    // 쿠키 만료 설정: 같은 도메인/SameSite로 제거
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
    // 요청 출처 확인
    boolean fromLocalhost = false;
    if (request != null) {
      String origin = request.getHeader("Origin");
      String referer = request.getHeader("Referer");
      String target = origin != null ? origin : referer;
      fromLocalhost =
          target != null && (target.contains("localhost") || target.contains("127.0.0.1"));
    }

    boolean isLocal = env.matchesProfiles("local");

    // 쿠키 설정 결정 (추가할 때와 동일해야 함)
    String sameSite;
    boolean secure;
    String domain;

    if (fromLocalhost) {
      sameSite = "None";
      secure = true;
      domain = null;
    } else if (isLocal) {
      sameSite = "Lax";
      secure = false;
      domain = null;
    } else {
      sameSite = "None";
      secure = true;
      domain =
          cookieDomain != null && !cookieDomain.isBlank()
              ? (cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain)
              : null;
    }

    CookieUtils.addCookie(
        response, ACCESS_TOKEN_COOKIE_NAME, null, 0, "/", true, secure, sameSite, domain);
    CookieUtils.addCookie(
        response, REFRESH_TOKEN_COOKIE_NAME, null, 0, "/", true, secure, sameSite, domain);

    log.debug(
        "[User] 쿠키 제거: domain={}, fromLocalhost={}",
        domain != null ? domain : "(host-only)",
        fromLocalhost);
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

  // --- 공통 헬퍼: 현재 요청 가져오기 ---
  private HttpServletRequest resolveRequest(HttpServletRequest request) {
    if (request != null) return request;
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs != null ? attrs.getRequest() : null;
  }

  // --- 공통: Admin 쿠키 설정 계산 ---
  private CookieSettings resolveAdminSettings(HttpServletRequest request) {
    boolean isLocal = env.matchesProfiles("local");
    boolean isDev = env.matchesProfiles("blue", "green", "dev");
    boolean fromLocalhost = false;
    if (request != null) {
      String origin = request.getHeader("Origin");
      String referer = request.getHeader("Referer");
      String target = origin != null ? origin : referer;
      fromLocalhost =
          target != null && (target.contains("localhost") || target.contains("127.0.0.1"));
    }

    String sameSite;
    boolean secure;
    if (fromLocalhost) {
      sameSite = "None";
      secure = true;
    } else if (isLocal) {
      sameSite = "Lax";
      secure = false;
    } else if (isDev) {
      sameSite = "Lax";
      secure = true;
    } else {
      sameSite = "Strict";
      secure = true;
    }

    String domain = null;
    if (!isLocal && adminCookieDomain != null && !adminCookieDomain.isBlank()) {
      domain =
          adminCookieDomain.startsWith(".") ? adminCookieDomain.substring(1) : adminCookieDomain;
    }

    return new CookieSettings(sameSite, secure, domain, fromLocalhost);
  }

  // 설정 값 묶음
  private record CookieSettings(
      String sameSite, boolean secure, String domain, boolean fromLocalhost) {}
}
