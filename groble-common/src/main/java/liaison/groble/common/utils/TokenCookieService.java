package liaison.groble.common.utils;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
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
    // HttpServletRequest를 받도록 메서드 시그니처 변경이 필요합니다
    addAdminTokenCookies(null, response, accessToken, refreshToken);
  }

  public void addAdminTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken) {

    // 기존 관리자 쿠키 제거 (중복 방지) - request 파라미터 전달
    clearAdminTokenCookies(request, response);

    // 현재 실행 환경 확인
    boolean isLocal = env.matchesProfiles("local");
    boolean isDev = env.matchesProfiles("blue", "green", "dev");
    boolean isProd = env.matchesProfiles("prod");

    // Origin과 Referer 헤더 모두 확인하여 localhost 요청 판단
    boolean isFromLocalhost = false;
    String requestOrigin = "unknown";
    if (request != null) {
      String origin = request.getHeader("Origin");
      String referer = request.getHeader("Referer");
      requestOrigin = origin != null ? origin : (referer != null ? referer : "unknown");

      // Origin 또는 Referer에 localhost가 포함되어 있는지 확인
      isFromLocalhost =
          requestOrigin.contains("localhost")
              || requestOrigin.contains("127.0.0.1")
              || requestOrigin.contains("0.0.0.0");

      log.debug(
          "요청 정보 - Origin: {}, Referer: {}, localhost 요청: {}", origin, referer, isFromLocalhost);
    }

    // 쿠키 설정 결정 로직
    String sameSite;
    boolean isSecure;
    String domain = null;

    if (isFromLocalhost) {
      // localhost에서의 요청: cross-origin 쿠키 설정
      sameSite = "None"; // Cross-origin 허용
      isSecure = true; // SameSite=None은 Secure 필수
      domain = null; // 도메인 설정하지 않음 (중요!)
      log.info("localhost에서의 요청 감지: 도메인 설정 없이 쿠키 생성");
    } else if (isLocal) {
      // 서버가 로컬에서 실행 중
      sameSite = "Lax";
      isSecure = false;
      domain = null;
    } else if (isDev) {
      // 개발 서버에서 실행 중 (같은 도메인 간 요청)
      sameSite = "Lax";
      isSecure = true;
      domain = adminCookieDomain; // dev.groble.im
    } else {
      // 프로덕션 환경
      sameSite = "Strict";
      isSecure = true;
      domain = adminCookieDomain; // groble.im
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
        "관리자 쿠키 설정 완료: profile={}, origin={}, domain={}, secure={}, sameSite={}, fromLocalhost={}",
        String.join(",", env.getActiveProfiles()),
        request != null ? request.getHeader("Origin") : "unknown",
        domain != null ? domain : "서버 도메인만",
        isSecure,
        sameSite,
        isFromLocalhost);
  }

  public void addTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken) {

    // 기존 관리자 쿠키 제거 (중복 방지) - request 파라미터 전달
    clearAdminTokenCookies(request, response);

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
  public void clearAdminTokenCookies(HttpServletRequest request, HttpServletResponse response) {
    boolean isLocal = env.matchesProfiles("local");
    boolean isDev = env.matchesProfiles("blue", "green", "dev");

    // 요청 출처 확인
    boolean isFromLocalhost = false;
    if (request != null) {
      String origin = request.getHeader("Origin");
      String referer = request.getHeader("Referer");
      String requestOrigin = origin != null ? origin : (referer != null ? referer : "");
      isFromLocalhost = requestOrigin.contains("localhost") || requestOrigin.contains("127.0.0.1");
    }

    String sameSite;
    String domain = null;
    boolean isSecure;

    if (isFromLocalhost) {
      // localhost 요청: 도메인 설정 없이 제거
      sameSite = "None";
      isSecure = true;
      domain = null;
    } else if (isLocal) {
      sameSite = "Lax";
      isSecure = false;
      domain = null;
    } else if (isDev) {
      sameSite = "Lax";
      isSecure = true;
      domain = adminCookieDomain;
    } else {
      sameSite = "Strict";
      isSecure = true;
      domain = adminCookieDomain;
    }

    // 액세스 토큰 쿠키 제거
    CookieUtils.addCookie(
        response, ACCESS_TOKEN_COOKIE_NAME, null, 0, "/", true, isSecure, sameSite, domain);

    // 리프레시 토큰 쿠키 제거
    CookieUtils.addCookie(
        response, REFRESH_TOKEN_COOKIE_NAME, null, 0, "/", true, isSecure, sameSite, domain);

    log.debug(
        "관리자 토큰 쿠키 제거 완료: domain={}, fromLocalhost={}",
        domain != null ? domain : "서버 도메인만",
        isFromLocalhost);
  }

  // 기존 메서드 오버로드 (하위 호환성 유지)
  public void clearAdminTokenCookies(HttpServletResponse response) {
    clearAdminTokenCookies(null, response);
  }

  /** 일반 사용자 토큰 쿠키들을 제거하여 중복 설정 방지 */
  public void clearTokenCookies(HttpServletResponse response) {
    boolean isLocal = env.matchesProfiles("local");
    String sameSite = isLocal ? "Lax" : "None";
    String domain = null;

    if (!isLocal) {
      // 일반 사용자 도메인 사용
      domain = cookieDomain != null && !cookieDomain.isBlank() ? cookieDomain : "groble.im";
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
