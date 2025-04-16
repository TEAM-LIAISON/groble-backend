package liaison.groble.security.oauth2.handler;

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

import liaison.groble.common.utils.CookieUtils;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.security.jwt.JwtTokenProvider;
import liaison.groble.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import liaison.groble.security.service.OAuth2AuthService.CustomOAuth2User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.cookie.domain}")
  private String cookieDomain;

  // 쿠키 설정값
  private static final int ACCESS_TOKEN_MAX_AGE = 60 * 30; // 30분
  private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7; // 7일
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  public OAuth2AuthenticationSuccessHandler(
      JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    log.info("OAuth2 인증 성공 - 처리 시작");

    // 쿠키에서 redirect_uri 확인 로깅
    Optional<String> redirectUri =
        CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue);
    log.info("리다이렉트 URI 존재: {}, 값: {}", redirectUri.isPresent(), redirectUri.orElse("없음"));

    // OAuth2 사용자 정보 가져오기
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    Long userId = oAuth2User.getId();

    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

    // 직접 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

    // 리프레시 토큰 저장
    user.updateRefreshToken(refreshToken);
    userRepository.save(user);

    log.info("사용자 인증 완료: {}, 토큰 발급 완료", oAuth2User.getEmail());

    // determineTargetUrl 메서드를 수정하여 인증 객체도 전달
    String targetUrl = determineTargetUrl(request, response, authentication);

    // 토큰을 쿠키에 저장
    addTokenCookies(response, accessToken, refreshToken);

    // 인증 속성 정리
    clearAuthenticationAttributes(request);

    // 프론트엔드로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  /** 리다이렉트 URL 결정 */
  protected String determineTargetUrl(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    // 쿠키에서 redirect_uri 값 확인
    Optional<String> redirectUri =
        CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue);

    // 토큰은 쿠키에만 저장하고 URL 파라미터에는 추가하지 않음
    return redirectUri.filter(uri -> !uri.isEmpty()).orElse(frontendUrl);
  }

  /** 리다이렉트 URI 유효성 검증 */
  private boolean isValidRedirectUri(String uri) {
    try {
      URI redirectUri = new URI(uri);

      // 프론트엔드 도메인에 속하는지 검증
      String host = redirectUri.getHost();
      return host != null && (host.equals("localhost") || host.endsWith("groble.im"));

    } catch (Exception e) {
      return false;
    }
  }

  private void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {

    boolean isSecure = !isLocalEnvironment();

    // 리다이렉트 URI의 도메인을 분석하여 쿠키 도메인 설정
    String domain = null;

    // 운영 환경에서는 브라우저와 공유할 수 있도록 상위 도메인 설정
    // 예: dev.groble.im, groble.im 등
    if (isProduction() || isDevelopment()) {
      domain = cookieDomain; // 설정에서 가져오기
    }

    // Access Token - HttpOnly 설정
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        "None",
        domain);

    // Refresh Token
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        "None",
        domain);

    log.debug(
        "토큰 쿠키 추가 완료: domain={}, accessToken({}초), refreshToken({}초)",
        domain != null ? domain : "localhost",
        ACCESS_TOKEN_MAX_AGE,
        REFRESH_TOKEN_MAX_AGE);
  }

  /**
   * 로컬 환경인지 확인
   *
   * @return 로컬 환경이면 true
   */
  private boolean isLocalEnvironment() {
    String activeProfile = getActiveProfile();
    return activeProfile.equals("local") || activeProfile.isEmpty();
  }

  /**
   * 운영 환경인지 확인
   *
   * @return 운영 환경이면 true
   */
  private boolean isProduction() {
    String activeProfile = getActiveProfile();
    return activeProfile.equals("prod") || activeProfile.equals("production");
  }

  /**
   * 개발 환경인지 확인
   *
   * @return 개발 환경이면 true
   */
  private boolean isDevelopment() {
    String activeProfile = getActiveProfile();
    return activeProfile.equals("dev") || activeProfile.equals("development");
  }

  /**
   * 현재 활성화된 프로필 가져오기
   *
   * @return 활성화된 프로필 (기본값: "")
   */
  private String getActiveProfile() {
    return System.getProperty("spring.profiles.active", "");
  }
}
