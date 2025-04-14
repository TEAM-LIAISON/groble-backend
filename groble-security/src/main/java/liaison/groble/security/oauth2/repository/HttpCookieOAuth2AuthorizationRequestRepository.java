package liaison.groble.security.oauth2.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.common.utils.CookieUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 요청을 쿠키에 저장하는 리포지토리 */
@Slf4j
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
  private final ObjectMapper objectMapper;

  public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
  public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  /** 요청에서 OAuth2 인증 요청 정보 로드 */
  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return CookieUtils.getSerializedCookie(
            request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, OAuth2AuthorizationRequest.class)
        .orElse(null);
  }

  /** OAuth2 인증 요청 정보 저장 */
  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authorizationRequest == null) {
      removeAuthorizationRequestCookies(request, response);
      return;
    }

    try {
      CookieUtils.addSerializedCookie(
          response,
          OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
          authorizationRequest,
          COOKIE_EXPIRE_SECONDS);

      String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
      if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isEmpty()) {
        CookieUtils.addCookie(
            response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
      }
    } catch (Exception e) {
      log.error("OAuth2 인증 요청 저장 중 오류 발생", e);
    }
  }

  /** OAuth2 인증 요청 정보 삭제 */
  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
    removeAuthorizationRequestCookies(request, response);
    return authRequest;
  }

  /** OAuth2 인증 요청 관련 쿠키 삭제 */
  public static void removeAuthorizationRequestCookies(
      HttpServletRequest request, HttpServletResponse response) {
    if (response != null) {
      CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
      CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
  }
}
