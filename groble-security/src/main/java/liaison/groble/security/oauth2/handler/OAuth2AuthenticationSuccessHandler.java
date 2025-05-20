package liaison.groble.security.oauth2.handler;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import liaison.groble.common.exception.EntityNotFoundException;
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

  private final Environment environment;
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

  @Autowired
  public OAuth2AuthenticationSuccessHandler(
      JwtTokenProvider jwtTokenProvider, UserRepository userRepository, Environment environment) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
    this.environment = environment;
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
    log.info("OAuth2 사용자 정보: ID={}, 이메일={}", userId, oAuth2User.getEmail());

    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 직접 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());
    Instant refreshTokenExpiresAt = jwtTokenProvider.getRefreshTokenExpirationInstant(refreshToken);

    // 리프레시 토큰 저장
    user.updateRefreshToken(refreshToken, refreshTokenExpiresAt);
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

    // 현재 활성화된 프로필에 따른 환경 설정
    boolean isLocal = isLocalEnvironment();

    // 개발 및 운영 환경에서는 HTTPS 사용하므로 Secure=true
    boolean isSecure = !isLocal;

    // SameSite 설정: OAuth2 리다이렉트 처리를 위해 'None' 설정 필수
    // 브라우저 요구사항: SameSite=None인 경우 항상 Secure=true여야 함
    String sameSite = "None";
    if (sameSite.equals("None")) {
      isSecure = true;
    }

    // 도메인 설정: 로컬 환경에서는 설정하지 않음
    // 개발/운영 환경에서는 app.cookie.domain 설정 사용 (groble.im)
    String domain = null;
    if (!isLocal) {
      domain = cookieDomain;
    }

    // Access Token
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true, // httpOnly
        isSecure, // secure
        sameSite, // sameSite
        domain); // domain

    // Refresh Token
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true, // httpOnly
        isSecure, // secure
        sameSite, // sameSite
        domain); // domain

    String activeProfile = getActiveProfile();
    log.debug(
        "OAuth2 토큰 쿠키 추가 완료: env={}, domain={}, secure={}, sameSite={}",
        activeProfile,
        domain != null ? domain : "기본값(localhost)",
        isSecure,
        sameSite);
  }

  private boolean isLocalEnvironment() {
    return Arrays.asList(environment.getActiveProfiles()).contains("local");
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
