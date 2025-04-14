package liaison.groble.security.oauth2.handler;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.common.utils.CookieUtils;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.security.jwt.JwtTokenProvider;
import liaison.groble.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import liaison.groble.security.service.OAuth2AuthService.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;

  // 프론트엔드 redirect URI (환경에 따라 설정 필요)
  private final String frontendRedirectUri =
      "https://api.dev.groble.im/api/v1/oauth2/login/success";

  // 허용된 리다이렉트 URI 목록
  private static final String[] ALLOWED_REDIRECT_URIS = {
    "http://localhost:3000/auth/sign-in", "https://dev.groble.im/auth/sign-in"
  };

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

    // 직접 토큰 생성 (OAuth2AuthService에 의존하지 않음)
    String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

    // 리프레시 토큰 저장
    user.updateRefreshToken(refreshToken);
    userRepository.save(user);

    log.info("사용자 인증 완료: {}, 토큰 발급 완료", oAuth2User.getEmail());

    // 토큰을 쿠키에 저장 (보안을 위해 httpOnly 설정)
    int accessTokenExpiry = 3600; // 1시간 (JwtTokenProvider와 일치하게 설정)
    int refreshTokenExpiry = 7 * 24 * 3600; // 7일 (JwtTokenProvider와 일치하게 설정)

    CookieUtils.addCookie(
        response, "access_token", accessToken, accessTokenExpiry, "/", true, false, "Lax");
    CookieUtils.addCookie(
        response, "refresh_token", refreshToken, refreshTokenExpiry, "/", true, false, "Lax");

    // 쿠키에서 redirect_uri 가져오기
    String targetUrl =
        determineTargetUrl(request, response, authentication, accessToken, refreshToken);

    // 인증 속성 정리
    clearAuthenticationAttributes(request);

    // 프론트엔드로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  /** 리다이렉트 URL 결정 */
  protected String determineTargetUrl(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication,
      String accessToken,
      String refreshToken) {

    // 쿠키에서 redirect_uri 값 확인
    Optional<String> redirectUri =
        CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue);

    String targetUrl = redirectUri.orElse(frontendRedirectUri);

    // 올바른 URI인지 검증 (필요시 화이트리스트 로직 추가)
    if (!isValidRedirectUri(targetUrl)) {
      log.error("유효하지 않은 리다이렉트 URI: {}", targetUrl);
      targetUrl = frontendRedirectUri;
    }

    // URI에 토큰 파라미터 추가
    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("token", accessToken)
        .queryParam("refresh_token", refreshToken)
        .build()
        .toUriString();
  }

  /** 리다이렉트 URI 유효성 검증 필요시 화이트리스트 로직을 추가할 수 있음 */
  private boolean isValidRedirectUri(String uri) {
    try {
      URI redirectUri = new URI(uri);

      // 화이트리스트 기반 URI 검증
      for (String allowedUri : ALLOWED_REDIRECT_URIS) {
        if (uri.startsWith(allowedUri)) {
          return true;
        }
      }

      // 기본 리다이렉트 URI인 경우도 허용
      return uri.equals(frontendRedirectUri);
    } catch (Exception e) {
      return false;
    }
  }
}
