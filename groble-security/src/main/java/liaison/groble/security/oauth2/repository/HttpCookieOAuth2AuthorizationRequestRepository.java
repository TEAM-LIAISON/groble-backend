package liaison.groble.security.oauth2.repository;

import java.util.Base64;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import liaison.groble.common.utils.CookieUtils;
import liaison.groble.security.oauth2.jackson.OAuth2AuthorizationRequestDeserializer;

import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 요청을 쿠키에 저장하고 불러오는 저장소 클래스 Spring Security의 AuthorizationRequestRepository 인터페이스 구현 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
  public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  private final ObjectMapper objectMapper;

  // 생성자에서 ObjectMapper 설정
  public HttpCookieOAuth2AuthorizationRequestRepository() {
    this.objectMapper = new ObjectMapper();

    // Java 8 날짜/시간 모듈 등록
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // OAuth2AuthorizationRequest 용 커스텀 역직렬화 모듈 등록
    SimpleModule module = new SimpleModule();
    module.addDeserializer(
        OAuth2AuthorizationRequest.class, new OAuth2AuthorizationRequestDeserializer());
    objectMapper.registerModule(module);
  }

  @Value("${app.cookie.domain}")
  private String cookieDomain;

  /**
   * HTTP 요청에서 OAuth2 인증 요청 정보 불러오기
   *
   * @param request HTTP 요청
   * @return OAuth2 인증 요청 객체
   */
  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    Optional<Cookie> cookie =
        CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);

    return cookie.map(this::deserializeAuthorizationRequest).orElse(null);
  }

  /**
   * OAuth2 인증 요청을 저장하고 리다이렉트 URI 쿠키 유지
   *
   * @param authorizationRequest OAuth2 인증 요청 객체
   * @param request HTTP 요청
   * @param response HTTP 응답
   */
  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authorizationRequest == null) {
      CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
      CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
      return;
    }

    // 환경에 따른 설정 결정
    String activeProfile = System.getProperty("spring.profiles.active", "local");
    boolean isLocal = activeProfile.contains("local") || activeProfile.isEmpty();

    // 개발/운영 환경에서는 HTTPS 사용하므로 Secure=true
    boolean isSecure = !isLocal;

    // OAuth2 리다이렉트 처리를 위해 SameSite=None 설정 필수
    // SameSite=None일 때는 항상 Secure=true 설정 (브라우저 요구사항)
    String sameSite = "None";
    if (sameSite.equals("None")) {
      isSecure = true;
    }

    // 도메인 설정: 로컬 환경에서는 설정하지 않음
    String domain = null;
    if (!isLocal) {
      domain = cookieDomain; // app.cookie.domain 속성값 (groble.im)
    }

    // OAuth2 인증 요청 정보를 쿠키에 저장
    String serializedAuthRequest = serializeAuthorizationRequest(authorizationRequest);
    CookieUtils.addCookie(
        response,
        OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
        serializedAuthRequest,
        COOKIE_EXPIRE_SECONDS,
        "/",
        true,
        isSecure,
        sameSite,
        domain);

    // 리다이렉트 URI 파라미터가 있으면 쿠키에 저장 (JavaScript에서 접근 가능하도록 httpOnly=false)
    String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
    if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isEmpty()) {
      CookieUtils.addCookie(
          response,
          REDIRECT_URI_PARAM_COOKIE_NAME,
          redirectUriAfterLogin,
          COOKIE_EXPIRE_SECONDS,
          "/",
          false, // JavaScript에서 접근 가능하도록 httpOnly=false
          isSecure,
          sameSite,
          domain);

      log.debug(
          "리다이렉트 URI 쿠키 저장: {}, domain={}, secure={}, sameSite={}",
          redirectUriAfterLogin,
          domain != null ? domain : "기본값(localhost)",
          isSecure,
          sameSite);
    }

    // 이미 존재하는 리다이렉트 URI 쿠키 확인 및 유지
    CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
        .ifPresent(
            cookie -> {
              log.debug("기존 리다이렉트 URI 쿠키 유지: {}", cookie.getValue());
            });

    log.debug(
        "OAuth2 인증 요청 저장 완료: domain={}, secure={}, sameSite={}",
        domain != null ? domain : "기본값(localhost)",
        isSecure,
        sameSite);
  }

  /**
   * OAuth2 인증 요청을 제거하고 관련 쿠키 삭제 리다이렉트 URI 쿠키는 유지
   *
   * @param request HTTP 요청
   * @return 제거된 OAuth2 인증 요청 객체
   */
  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);

    // 인증 요청 쿠키만 삭제하고 리다이렉트 URI 쿠키는 유지
    if (response != null) {
      CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    return authRequest;
  }

  /**
   * OAuth2 인증 관련 모든 쿠키 제거
   *
   * @param request HTTP 요청
   * @param response HTTP 응답
   */
  public void removeAuthorizationRequestCookies(
      HttpServletRequest request, HttpServletResponse response) {
    CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    log.debug("OAuth2 인증 쿠키 제거 완료");
  }

  /**
   * OAuth2 인증 요청 객체를 직렬화하여 문자열로 변환
   *
   * @param authorizationRequest OAuth2 인증 요청 객체
   * @return 직렬화된 문자열
   */
  private String serializeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(authorizationRequest);
      return Base64.getUrlEncoder().encodeToString(bytes);
    } catch (Exception e) {
      log.error("OAuth2 인증 요청 직렬화 중 오류 발생", e);
      throw new IllegalStateException("OAuth2 인증 요청을 직렬화할 수 없습니다.", e);
    }
  }

  /**
   * 직렬화된 문자열에서 OAuth2 인증 요청 객체로 역직렬화
   *
   * @param cookie 쿠키 객체
   * @return OAuth2 인증 요청 객체
   */
  private OAuth2AuthorizationRequest deserializeAuthorizationRequest(Cookie cookie) {
    try {
      byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
      return objectMapper.readValue(bytes, OAuth2AuthorizationRequest.class);
    } catch (Exception e) {
      log.error("OAuth2 인증 요청 역직렬화 중 오류 발생: {}", e.getMessage());

      // 역직렬화 실패 시 null 반환 - 예외를 던지지 않고 빈 인증 요청으로 처리
      return null;
    }
  }
}
