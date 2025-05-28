package liaison.groble.common.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/** HTTP 쿠키 관리를 위한 유틸리티 클래스. 쿠키 생성, 조회, 삭제 및 직렬화된 객체 저장 기능을 제공합니다. */
@Slf4j
public class CookieUtils {
  // 기본 쿠키 설정값
  private static final String DEFAULT_PATH = "/";
  private static final boolean DEFAULT_HTTP_ONLY = true;
  private static final boolean DEFAULT_SECURE_DEV = false; // 개발 환경
  private static final boolean DEFAULT_SECURE_PROD = true; // 운영 환경
  private static final String DEFAULT_SAME_SITE = "Lax"; // 기본값을 Lax로 변경 (보안과 편의성 균형)

  /**
   * 요청에서 특정 이름의 쿠키 가져오기
   *
   * @param request HTTP 요청
   * @param name 쿠키 이름
   * @return 쿠키 Optional 객체
   */
  public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null && cookies.length > 0) {
      return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(name)).findFirst();
    }

    return Optional.empty();
  }

  /**
   * 응답에 쿠키 추가 (기본 설정 사용)
   *
   * @param response HTTP 응답
   * @param name 쿠키 이름
   * @param value 쿠키 값
   * @param maxAge 쿠키 유효 시간(초)
   */
  public static void addCookie(
      HttpServletResponse response, String name, String value, int maxAge) {
    addCookie(
        response,
        name,
        value,
        maxAge,
        DEFAULT_PATH,
        DEFAULT_HTTP_ONLY,
        isSecureEnvironment(),
        DEFAULT_SAME_SITE,
        null);
  }

  /**
   * 응답에 쿠키 추가 (상세 설정 사용)
   *
   * @param response HTTP 응답
   * @param name 쿠키 이름
   * @param value 쿠키 값
   * @param maxAge 쿠키 유효 시간(초)
   * @param path 쿠키 경로
   * @param httpOnly HttpOnly 플래그 설정
   * @param secure Secure 플래그 설정
   * @param sameSite SameSite 속성 (Lax, Strict, None)
   */
  public static void addCookie(
      HttpServletResponse response,
      String name,
      String value,
      int maxAge,
      String path,
      boolean httpOnly,
      boolean secure,
      String sameSite) {
    // 도메인 없이 쿠키 추가
    addCookie(response, name, value, maxAge, path, httpOnly, secure, sameSite, null);
  }

  /**
   * 응답에 쿠키 추가 (상세 설정 사용)
   *
   * @param response HTTP 응답
   * @param name 쿠키 이름
   * @param value 쿠키 값
   * @param maxAge 쿠키 유효 시간(초)
   * @param path 쿠키 경로
   * @param httpOnly HttpOnly 플래그 설정
   * @param secure Secure 플래그 설정
   * @param sameSite SameSite 속성 (Lax, Strict, None)
   * @param domain 쿠키 도메인 (null인 경우 현재 도메인)
   */
  public static void addCookie(
      HttpServletResponse response,
      String name,
      String value,
      int maxAge,
      String path,
      boolean httpOnly,
      boolean secure,
      String sameSite,
      String domain) {

    // SameSite=None인 경우 Secure 플래그 강제 설정 (브라우저 요구사항)
    if ("None".equalsIgnoreCase(sameSite)) {
      secure = true;
    }

    // Local 환경에서는 domain을 설정하지 않음 (localhost에서 동작하도록)
    boolean isLocalEnv = isLocalEnvironment();
    if (isLocalEnv) {
      domain = null;
    }

    // 기본 쿠키 생성
    Cookie cookie = new Cookie(name, value);
    cookie.setPath(path);
    cookie.setHttpOnly(httpOnly);
    cookie.setMaxAge(maxAge);
    cookie.setSecure(secure);

    // 도메인 설정 (null이 아닌 경우에만)
    if (domain != null && !domain.isEmpty() && !isLocalEnv) {
      cookie.setDomain(domain);
      log.debug("쿠키 도메인 설정: {}", domain);
    }

    // jakarta.servlet.http.Cookie에는 SameSite 설정이 없으므로 헤더로 추가
    StringBuilder cookieHeader = new StringBuilder();
    cookieHeader.append(String.format("%s=%s", name, value));
    cookieHeader.append(String.format("; Path=%s", path));
    cookieHeader.append(String.format("; Max-Age=%d", maxAge));

    if (httpOnly) {
      cookieHeader.append("; HttpOnly");
    }

    if (secure) {
      cookieHeader.append("; Secure");
    }

    if (domain != null && !domain.isEmpty() && !isLocalEnv) {
      cookieHeader.append(String.format("; Domain=%s", domain));
    }

    if (sameSite != null && !sameSite.isEmpty()) {
      cookieHeader.append(String.format("; SameSite=%s", sameSite));
    }

    // 기본 쿠키 추가 및 헤더 설정
    response.addCookie(cookie);
    response.addHeader("Set-Cookie", cookieHeader.toString());

    log.debug(
        "쿠키 추가: {}, domain={}, maxAge={}, secure={}, sameSite={}",
        name,
        domain,
        maxAge,
        secure,
        sameSite);
  }

  /**
   * 응답에서 쿠키 삭제
   *
   * @param request HTTP 요청
   * @param response HTTP 응답
   * @param name 삭제할 쿠키 이름
   */
  public static void deleteCookie(
      HttpServletRequest request, HttpServletResponse response, String name) {
    // 요청에 붙어있는 원본 쿠키 가져오기
    Optional<Cookie> maybe = getCookie(request, name);
    if (maybe.isEmpty()) {
      return;
    }

    Cookie original = maybe.get();

    // 삭제용 쿠키 객체 생성 (속성은 원본과 똑같이)
    Cookie cookie = new Cookie(name, "");
    cookie.setPath(original.getPath() != null ? original.getPath() : DEFAULT_PATH);
    cookie.setHttpOnly(original.isHttpOnly());
    cookie.setSecure(original.getSecure());
    cookie.setMaxAge(0);

    // Domain 이 설정되어 있었다면 동일하게 지정
    if (original.getDomain() != null) {
      cookie.setDomain(original.getDomain());
    }

    response.addCookie(cookie);

    // 중복 방지를 위해 헤더 방식도 추가
    StringBuilder header =
        new StringBuilder()
            .append(name)
            .append("=; Path=")
            .append(cookie.getPath())
            .append("; Max-Age=0");
    if (cookie.isHttpOnly()) {
      header.append("; HttpOnly");
    }
    if (cookie.getSecure()) {
      header.append("; Secure");
    }
    if (original.getDomain() != null) {
      header.append("; Domain=").append(original.getDomain());
    }
    // 원본 SameSite 값도 유지
    header.append("; SameSite=").append(DEFAULT_SAME_SITE);

    response.addHeader("Set-Cookie", header.toString());
  }

  public static void deleteCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String name,
      String path,
      String domain,
      String sameSite,
      boolean httpOnly,
      boolean secure) {
    // Path, Domain, SameSite, HttpOnly, Secure 값을 일치시켜 만료 쿠키 설정
    Cookie cookie = new Cookie(name, "");
    cookie.setPath(path);
    cookie.setHttpOnly(httpOnly);
    cookie.setSecure(secure);
    cookie.setMaxAge(0);
    if (domain != null && !domain.isEmpty()) {
      cookie.setDomain(domain);
    }
    response.addCookie(cookie);

    // 헤더 방식으로도 제거
    StringBuilder header = new StringBuilder();
    header.append(name).append("=; Path=").append(path).append("; Max-Age=0");
    if (httpOnly) header.append("; HttpOnly");
    if (secure) header.append("; Secure");
    if (domain != null && !domain.isEmpty()) header.append("; Domain=").append(domain);
    if (sameSite != null && !sameSite.isEmpty()) header.append("; SameSite=").append(sameSite);
    response.addHeader("Set-Cookie", header.toString());
  }

  /**
   * 도메인을 지정하여 쿠키 삭제 (편의성 메서드)
   *
   * @param request HTTP 요청
   * @param response HTTP 응답
   * @param name 삭제할 쿠키 이름
   * @param domain 쿠키 도메인 (null이면 현재 도메인)
   */
  public static void deleteCookieWithDomain(
      HttpServletRequest request, HttpServletResponse response, String name, String domain) {

    // 기본 설정값 사용
    deleteCookie(
        request,
        response,
        name,
        DEFAULT_PATH,
        domain,
        DEFAULT_SAME_SITE,
        DEFAULT_HTTP_ONLY,
        isSecureEnvironment());
  }

  /**
   * 직렬화된 객체를 쿠키에 저장
   *
   * @param response HTTP 응답
   * @param name 쿠키 이름
   * @param obj 저장할 객체 (java.io.Serializable 구현 필요)
   * @param maxAge 쿠키 유효 시간(초)
   */
  public static void addSerializedCookie(
      HttpServletResponse response, String name, Object obj, int maxAge) {
    try {
      String serialized = Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
      addCookie(response, name, serialized, maxAge);
    } catch (Exception e) {
      log.error("객체 직렬화 중 오류 발생", e);
      throw new CookieSerializationException("객체 직렬화 중 오류 발생", e);
    }
  }

  /**
   * 쿠키에서 직렬화된 객체 가져오기
   *
   * @param request HTTP 요청
   * @param name 쿠키 이름
   * @param cls 객체 클래스
   * @return 역직렬화된 객체
   * @param <T> 반환할 객체 타입
   */
  public static <T> Optional<T> getSerializedCookie(
      HttpServletRequest request, String name, Class<T> cls) {
    return getCookie(request, name)
        .map(
            cookie -> {
              try {
                byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
                return SerializationUtils.deserialize(bytes, cls);
              } catch (Exception e) {
                log.error("객체 역직렬화 중 오류 발생: {}", name, e);
                throw new CookieSerializationException("객체 역직렬화 중 오류 발생", e);
              }
            });
  }

  /**
   * 현재 환경이 보안 환경(운영 또는 개발)인지 확인
   *
   * @return 보안 환경 여부
   */
  private static boolean isSecureEnvironment() {
    String env = System.getProperty("spring.profiles.active", "local");
    // 운영 또는 개발 환경인 경우 true 반환 (HTTPS 사용)
    return env.contains("prod")
        || env.contains("dev")
        || env.contains("blue")
        || env.contains("green");
  }

  /**
   * 현재 환경이 로컬 환경인지 확인
   *
   * @return 로컬 환경 여부
   */
  private static boolean isLocalEnvironment() {
    String env = System.getProperty("spring.profiles.active", "local");
    return env.contains("local") || env.isEmpty();
  }

  /** 쿠키 직렬화 관련 예외 클래스 */
  public static class CookieSerializationException extends RuntimeException {
    public CookieSerializationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
