package liaison.groble.common.utils;

import java.time.Duration;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

  public void addAdminTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {

    // 기존 관리자 쿠키 제거 (중복 방지)
    clearAdminTokenCookies(response);

    boolean isLocal = env.matchesProfiles("local");
    boolean isDev = env.matchesProfiles("blue", "green", "dev");
    boolean isProd = env.matchesProfiles("prod");

    // 환경별로 sameSite 설정을 달리 함
    // 로컬: Lax (localhost에서만 사용)
    // 개발: Lax (같은 dev.groble.im 도메인 내에서 사용)
    // 프로덕션: Strict (보안 강화)
    String sameSite;
    if (isLocal) {
      sameSite = "Lax";
    } else if (isDev) {
      sameSite = "Lax";
    } else {
      sameSite = "Strict";
    }

    // HTTPS는 로컬을 제외한 모든 환경에서 필수
    boolean isSecure = !isLocal;

    // 도메인 설정
    // 로컬: 도메인 설정 없음 (localhost에서만 동작)
    // 개발/프로덕션: 설정 파일의 admin-domain 사용
    String domain = null;
    if (!isLocal) {
      domain =
          adminCookieDomain != null && !adminCookieDomain.isBlank()
              ? adminCookieDomain
              : ".groble.im"; // 기본값은 프로덕션 도메인
    }

    // 액세스 토큰 쿠키 설정
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        sameSite,
        domain);

    // 리프레시 토큰 쿠키 설정
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        sameSite,
        domain);

    log.info(
        "관리자 쿠키 설정 완료: profile={}, domain={}, secure={}, sameSite={}",
        String.join(",", env.getActiveProfiles()),
        domain != null ? domain : "localhost",
        isSecure,
        sameSite);
  }

  public void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {

    // 기존 관리자 쿠키 제거 (중복 방지)
    clearAdminTokenCookies(response);

    boolean isLocal = env.matchesProfiles("local");
    String sameSite = isLocal ? "Lax" : "None";
    boolean isSecure = !isLocal;

    String domain = null;
    if (!isLocal && cookieDomain != null && !cookieDomain.isBlank()) {
      domain = cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain;
    }

    // 액세스 토큰 쿠키 설정
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        sameSite,
        domain);

    // 리프레시 토큰 쿠키 설정
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        sameSite,
        domain);

    log.info(
        "쿠키 설정 완료: profile={}, domain={}, secure={}, sameSite={}",
        String.join(",", env.getActiveProfiles()),
        domain != null ? domain : "localhost",
        isSecure,
        sameSite);
  }

  /** 관리자 토큰 쿠키들을 제거하여 중복 설정 방지 */
  public void clearAdminTokenCookies(HttpServletResponse response) {
    boolean isLocal = env.matchesProfiles("local");
    boolean isDev = env.matchesProfiles("blue", "green", "dev");

    String sameSite;
    if (isLocal) {
      sameSite = "Lax";
    } else if (isDev) {
      sameSite = "Lax";
    } else {
      sameSite = "Strict";
    }

    String domain = null;
    if (!isLocal) {
      // 관리자 도메인 사용
      domain =
          adminCookieDomain != null && !adminCookieDomain.isBlank()
              ? adminCookieDomain
              : ".groble.im";
    }

    // 액세스 토큰 쿠키 제거
    CookieUtils.addCookie(
        response, ACCESS_TOKEN_COOKIE_NAME, null, 0, "/", true, !isLocal, sameSite, domain);

    // 리프레시 토큰 쿠키 제거
    CookieUtils.addCookie(
        response, REFRESH_TOKEN_COOKIE_NAME, null, 0, "/", true, !isLocal, sameSite, domain);

    log.debug("관리자 토큰 쿠키 제거 완료: domain={}", domain != null ? domain : "localhost");
  }

  /** 일반 사용자 토큰 쿠키들을 제거하여 중복 설정 방지 */
  public void clearTokenCookies(HttpServletResponse response) {
    boolean isLocal = env.matchesProfiles("local");
    String sameSite = isLocal ? "Lax" : "None";
    String domain = null;

    if (!isLocal) {
      // 일반 사용자 도메인 사용
      domain = cookieDomain != null && !cookieDomain.isBlank() ? cookieDomain : ".groble.im";
    }

    // 액세스 토큰 쿠키 제거
    CookieUtils.addCookie(
        response, ACCESS_TOKEN_COOKIE_NAME, null, 0, "/", true, !isLocal, sameSite, domain);

    // 리프레시 토큰 쿠키 제거
    CookieUtils.addCookie(
        response, REFRESH_TOKEN_COOKIE_NAME, null, 0, "/", true, !isLocal, sameSite, domain);

    log.debug("기존 토큰 쿠키 제거 완료: domain={}", domain != null ? domain : "localhost");
  }

  /** 로그아웃 시 토큰 쿠키 완전 제거 */
  public void removeTokenCookies(HttpServletResponse response) {
    clearTokenCookies(response);
    log.info("토큰 쿠키 제거 완료");
  }

  /** 관리자 로그아웃 시 토큰 쿠키 완전 제거 */
  public void removeAdminTokenCookies(HttpServletResponse response) {
    clearAdminTokenCookies(response);
    log.info("관리자 토큰 쿠키 제거 완료");
  }
}
