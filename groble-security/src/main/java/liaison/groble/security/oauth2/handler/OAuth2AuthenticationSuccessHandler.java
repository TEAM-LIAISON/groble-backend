package liaison.groble.security.oauth2.handler;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.security.jwt.JwtTokenProvider;
import liaison.groble.security.service.OAuth2AuthService.CustomOAuth2User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final TokenCookieService tokenCookieService;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Autowired
  public OAuth2AuthenticationSuccessHandler(
      JwtTokenProvider jwtTokenProvider,
      UserRepository userRepository,
      TokenCookieService tokenCookieService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
    this.tokenCookieService = tokenCookieService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    log.info("OAuth2 인증 성공 - 처리 시작");

    // 세션에서 redirect_uri 확인 로깅
    String redirectUri = (String) request.getSession().getAttribute("redirect_uri");
    log.info(
        "리다이렉트 URI 존재: {}, 값: {}", redirectUri != null, redirectUri != null ? redirectUri : "없음");

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

    // TokenCookieService를 사용하여 토큰을 쿠키에 저장
    // TokenCookieService가 요청 출처를 자동으로 판단하여 적절한 쿠키 설정을 적용
    tokenCookieService.addTokenCookies(request, response, accessToken, refreshToken);

    // 세션에서 redirect_uri 제거
    request.getSession().removeAttribute("redirect_uri");

    // 인증 속성 정리
    clearAuthenticationAttributes(request);

    // 프론트엔드로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  /** 리다이렉트 URL 결정 */
  protected String determineTargetUrl(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    // 세션에서 redirect_uri 값 확인
    String redirectUri = (String) request.getSession().getAttribute("redirect_uri");

    // 토큰은 쿠키에만 저장하고 URL 파라미터에는 추가하지 않음
    return redirectUri != null && !redirectUri.isEmpty() ? redirectUri : frontendUrl;
  }
}
