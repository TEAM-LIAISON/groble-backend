package liaison.grobleauth.security.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import liaison.grobleauth.dto.AuthDto.TokenResponse;
import liaison.grobleauth.security.util.CookieUtils;
import liaison.grobleauth.service.OAuth2AuthService;
import liaison.grobleauth.service.OAuth2AuthService.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 성공 시 처리를 담당하는 핸들러 인증 후 JWT 토큰을 생성하고 프론트엔드로 리다이렉트 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Value("${app.oauth2.redirectUri}")
  private String redirectUri;

  @Value("${app.oauth2.cookieName}")
  private String cookieName;

  @Value("${app.oauth2.cookieExpireSeconds}")
  private int cookieExpireSeconds;

  private final OAuth2AuthService oAuth2AuthService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    log.info("OAuth2 인증 성공 - 리다이렉트 처리 시작");

    // 로그 출력을 통한 디버깅
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        log.info("쿠키 정보: {} = {}", cookie.getName(), cookie.getValue());
      }
    }

    // redirect_uri 쿠키에서 URI 추출 시도
    Optional<String> redirectUri =
        CookieUtils.getCookie(request, "redirect_uri").map(Cookie::getValue);

    // 쿠키가 없으면 기본 URI 사용
    String targetUrl;
    if (redirectUri.isPresent()) {
      log.info("쿠키에서 리다이렉트 URI 찾음: {}", redirectUri.get());
      targetUrl = redirectUri.get();
    } else {
      log.info("리다이렉트 URI 쿠키 없음, 기본 URI 사용");
      targetUrl = getDefaultTargetUrl();
    }

    // 일단 리다이렉트 URI 승인 검사 비활성화 (개발 중에만)
    // if (!isAuthorizedRedirectUri(targetUrl)) {
    //    log.error("승인되지 않은 리다이렉트 URI: {}", targetUrl);
    //    targetUrl = getDefaultTargetUrl();
    // }

    // OAuth2 사용자 정보 가져오기
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getEmail();

    // 토큰 생성
    TokenResponse tokenResponse = oAuth2AuthService.createTokens(email);

    // 프론트엔드로 토큰 정보와 함께 리다이렉트 URL 생성
    String finalRedirectUrl =
        UriComponentsBuilder.fromUriString(targetUrl)
            .queryParam("token", tokenResponse.getAccessToken())
            .queryParam("refresh_token", tokenResponse.getRefreshToken())
            .queryParam("expires_in", tokenResponse.getExpiresIn())
            .build()
            .toUriString();

    log.info("최종 리다이렉트 URL: {}", finalRedirectUrl);

    clearAuthenticationAttributes(request, response);
    getRedirectStrategy().sendRedirect(request, response, finalRedirectUrl);
  }

  /** 인증 성공 후 리다이렉트 URL 결정 */
  protected String determineTargetUrl(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    Optional<String> redirectUri =
        CookieUtils.getCookie(request, "redirect_uri").map(Cookie::getValue);

    if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
      log.error("승인되지 않은 리다이렉트 URI로 인해 리다이렉트할 수 없습니다: {}", redirectUri);
      throw new IllegalArgumentException("승인되지 않은 리다이렉트 URI입니다");
    }

    String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

    // OAuth2 사용자 정보 가져오기
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getEmail();

    // 토큰 생성
    TokenResponse tokenResponse = oAuth2AuthService.createTokens(email);

    // 프론트엔드로 토큰 정보와 함께 리다이렉트 URL 생성
    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("token", tokenResponse.getAccessToken())
        .queryParam("refresh_token", tokenResponse.getRefreshToken())
        .queryParam("expires_in", tokenResponse.getExpiresIn())
        .build()
        .toUriString();
  }

  /** 인증 속성 정리 및 쿠키 삭제 */
  protected void clearAuthenticationAttributes(
      HttpServletRequest request, HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    CookieUtils.deleteCookie(request, response, cookieName);
    CookieUtils.deleteCookie(request, response, "redirect_uri");
  }

  /** 승인된 리다이렉트 URI인지 확인 */
  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);
    URI authorizedRedirectUri = URI.create(redirectUri);

    // 도메인과 포트가 일치하는지 확인
    return authorizedRedirectUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
        && authorizedRedirectUri.getPort() == clientRedirectUri.getPort();
  }
}
