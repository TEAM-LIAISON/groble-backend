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

    // 쿠키에서 redirect_uri 가져오기
    String targetUrl = determineTargetUrl(request, response);

    // 토큰을 쿠키에 저장
    addTokenCookies(response, accessToken, refreshToken);

    // 인증 속성 정리
    clearAuthenticationAttributes(request);

    // 프론트엔드로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  /** 리다이렉트 URL 결정 */
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
    // 쿠키에서 redirect_uri 값 확인
    Optional<String> redirectUri =
        CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue);

    String targetUrl = redirectUri.orElse(frontendUrl + "/oauth2/redirect");

    // 올바른 URI인지 검증 (필요시 화이트리스트 로직 추가)
    if (!isValidRedirectUri(targetUrl)) {
      log.error("유효하지 않은 리다이렉트 URI: {}", targetUrl);
      targetUrl = frontendUrl + "/oauth2/redirect";
    }

    return targetUrl;
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

  /** 액세스 토큰과 리프레시 토큰을 쿠키에 저장 */
  private void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {

    boolean isSecure = isSecureEnvironment();

    // Access Token - HttpOnly 설정 (JS에서 접근 불가)
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        "None",
        cookieDomain);

    // Refresh Token - HttpOnly 설정 (JS에서 접근 불가, 보안 강화)
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        isSecure,
        "None",
        cookieDomain);

    log.debug(
        "토큰 쿠키 추가 완료: domain={}, accessToken({}초), refreshToken({}초)",
        cookieDomain,
        ACCESS_TOKEN_MAX_AGE,
        REFRESH_TOKEN_MAX_AGE);
  }

  /** 보안 환경(운영)인지 확인 */
  private boolean isSecureEnvironment() {
    String env = System.getProperty("spring.profiles.active", "dev");
    return env.equalsIgnoreCase("prod") || env.equalsIgnoreCase("production");
  }
}
