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

  @Value("${app.cookie.domain:}") // 기본값을 빈 문자열로 설정
  private String cookieDomain;

  @Value("${app.cookie.admin-domain:}") // 기본값을 빈 문자열로 설정
  private String adminCookieDomain;

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
      // [시나리오] 백엔드 로컬 개발 (localhost:8080)
      // 프론트엔드도 localhost:3000 이므로, host-only 쿠키로 설정.
      // http 환경이므로 secure=false, SameSite=Lax
      sameSite = "Lax";
      secure = false;
      domain = null; // host-only cookie for localhost
    } else {
      // [시나리오] 개발/운영 서버 (api.dev.groble.im, api.groble.im)
      // 프론트엔드(dev.groble.im, groble.im) 및 로컬(localhost:3000)에서의 요청을 모두 처리해야 함.
      // 크로스-도메인/서브도메인 통신을 위해 SameSite=None, secure=true 로 설정.
      sameSite = "None";
      secure = true;
      domain = (cookieDomain != null && !cookieDomain.isBlank()) ? cookieDomain : null;
    }

    return new CookieSettings(sameSite, secure, domain, fromLocalhost);
  }

  // --- 공통: Admin 쿠키 설정 계산 ---
  private CookieSettings resolveAdminSettings(HttpServletRequest request) {
    HttpServletRequest req = resolveRequest(request);
    boolean fromLocalhost = isRequestFromLocalhost(req);
    boolean isLocalProfile = env.matchesProfiles("local");
    boolean isDevProfile = env.matchesProfiles("blue", "green", "dev");

    String sameSite;
    boolean secure;
    String domain = null;

    if (isLocalProfile) {
      // 백엔드가 로컬 환경에서 실행될 때
      sameSite = "Lax";
      secure = false;
    } else {
      // 백엔드가 개발 또는 운영 환경에서 실행될 때
      secure = true;
      domain =
          (adminCookieDomain != null && !adminCookieDomain.isBlank()) ? adminCookieDomain : null;
      if (fromLocalhost) {
        // 프론트엔드만 로컬일 경우 (localhost -> api.dev.groble.im)
        sameSite = "None";
      } else if (isDevProfile) {
        // 개발 환경 (dev.admin.groble.im -> api.dev.groble.im)
        sameSite = "Lax";
      } else {
        // 운영 환경 (admin.groble.im -> api.groble.im)
        sameSite = "None";
      }
    }

    return new CookieSettings(sameSite, secure, domain, fromLocalhost);
  }

  // 설정 값 묶음
  private record CookieSettings(
      String sameSite, boolean secure, String domain, boolean fromLocalhost) {}
}
