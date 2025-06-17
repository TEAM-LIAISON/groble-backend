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
  private String adminCookieDomain; // 쿠키 도메인 설정

  private static final int ACCESS_TOKEN_MAX_AGE = (int) Duration.ofHours(1).toSeconds();
  private static final int REFRESH_TOKEN_MAX_AGE = (int) Duration.ofDays(7).toSeconds();
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  public void addAdminTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {
    boolean isLocal = env.matchesProfiles("local");
    String sameSite = isLocal ? "Lax" : "None";
    boolean isSecure = !isLocal;

    String domain = null;
    if (!isLocal) {
      // 운영 도메인을 관리자용으로 고정
      domain = "admin.groble.im";
    }

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

    log.debug(
        "관리자 쿠키 설정: profile={}, domain={}, secure={}, sameSite={}",
        String.join(",", env.getActiveProfiles()),
        domain != null ? domain : "localhost",
        isSecure,
        sameSite);
  }

  public void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {

    boolean isLocal = env.matchesProfiles("local");
    String sameSite = isLocal ? "Lax" : "None";
    boolean isSecure = !isLocal; // 로컬은 http

    String domain = null;
    if (!isLocal && !cookieDomain.isBlank()) {
      domain = cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain;
    }

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

    log.debug(
        "쿠키 설정: profile={}, domain={}, secure={}, sameSite={}",
        String.join(",", env.getActiveProfiles()),
        domain != null ? domain : "localhost",
        isSecure,
        sameSite);
  }
}
