package liaison.grobleauth.security.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** OAuth2 인증 과정에서 상태 유지를 위한 쿠키 처리에 사용 */
public class CookieUtils {

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
   * 응답에 쿠키 추가
   *
   * @param response HTTP 응답
   * @param name 쿠키 이름
   * @param value 쿠키 값
   * @param maxAge 쿠키 유효 시간(초)
   */
  public static void addCookie(
      HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);

    // SameSite 속성을 헤더로 직접 설정
    String cookieHeader =
        String.format(
            "%s=%s; Path=%s; Max-Age=%d; HttpOnly; SameSite=Lax", name, value, "/", maxAge);
    response.addHeader("Set-Cookie", cookieHeader);
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
    Cookie[] cookies = request.getCookies();
    if (cookies != null && cookies.length > 0) {
      Arrays.stream(cookies)
          .filter(cookie -> cookie.getName().equals(name))
          .forEach(
              cookie -> {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
              });
    }
  }

  /**
   * 직렬화된 객체를 쿠키에 저장
   *
   * @param response HTTP 응답
   * @param name 쿠키 이름
   * @param obj 저장할 객체
   * @param maxAge 쿠키 유효 시간(초)
   */
  public static void addSerializedCookie(
      HttpServletResponse response, String name, Object obj, int maxAge) {
    try {
      String serialized = Base64.getEncoder().encodeToString(SerializationUtils.serialize(obj));
      addCookie(response, name, serialized, maxAge);
    } catch (Exception e) {
      throw new RuntimeException("객체 직렬화 중 오류 발생", e);
    }
  }

  /**
   * 쿠키에서 직렬화된 객체 가져오기
   *
   * @param request HTTP 요청
   * @param name 쿠키 이름
   * @param cls 객체 클래스
   * @return 역직렬화된 객체
   */
  public static <T> Optional<T> getSerializedCookie(
      HttpServletRequest request, String name, Class<T> cls) {
    return getCookie(request, name)
        .map(
            cookie -> {
              try {
                byte[] bytes = Base64.getDecoder().decode(cookie.getValue());
                return cls.cast(SerializationUtils.deserialize(bytes));
              } catch (Exception e) {
                throw new RuntimeException("객체 역직렬화 중 오류 발생", e);
              }
            });
  }
}
