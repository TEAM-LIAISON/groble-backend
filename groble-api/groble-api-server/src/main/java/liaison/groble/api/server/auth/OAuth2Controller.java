package liaison.groble.api.server.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.common.utils.CookieUtils;
import liaison.groble.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 관련 컨트롤러 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OAuth2", description = "OAuth2 소셜 로그인 관련 API")
public class OAuth2Controller {

  // 환경별 프론트엔드 도메인 설정
  @Value("${app.frontend-url}")
  private String frontendDomain; // 환경별로 설정 가능하도록 변경

  /** OAuth2 인증 페이지로 리다이렉트하기 전에 리다이렉트 URI를 쿠키에 저장 */
  @Operation(summary = "OAuth2 로그인 시작", description = "소셜 로그인 시작 전 리다이렉트 URI를 설정합니다.")
  @GetMapping("/authorize")
  public void authorize(
      @RequestParam(value = "redirect_uri", defaultValue = "/auth/sign-in") String redirectUri,
      @RequestParam("provider") String provider,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {

    // 상대 경로인 경우 프론트엔드 도메인을 앞에 추가
    if (redirectUri.startsWith("/")) {
      redirectUri = frontendDomain + redirectUri;
    }

    log.info("OAuth2 로그인 시작: provider={}, redirect_uri={}", provider, redirectUri);

    // 쿠키 세팅 - frontend redirect URI를 저장 (여기서 값이 제대로 저장되는지 확인)
    CookieUtils.addCookie(
        response,
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
        redirectUri,
        180);

    // Redirect to OAuth2 provider
    response.sendRedirect("/oauth2/authorize/" + provider);
  }

  /**
   * OAuth2 로그인 성공 후 임시 리다이렉트 페이지 OAuth2AuthenticationSuccessHandler는 이 페이지로 리다이렉트하도록 설정되어야 함 쿠키에
   * 저장된 토큰을 사용하여 프론트엔드로 리다이렉트
   */
  @Operation(summary = "OAuth2 로그인 완료", description = "OAuth2 인증 완료 후 프론트엔드로 리다이렉트합니다.")
  @GetMapping("/login/success")
  public void oauthLoginSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(value = "token", required = false) String token,
      @RequestParam(value = "refresh_token", required = false) String refreshToken)
      throws Exception {

    log.info("OAuth2 로그인 성공 처리: 토큰 있음={}", token != null);

    // 쿠키에서 원래 저장해둔 redirect_uri를 가져옴
    String redirectUri =
        CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue)
            .orElse(frontendDomain + "/auth/sign-in");

    // 쿠키 삭제
    CookieUtils.deleteCookie(
        request,
        response,
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME);

    // 토큰 정보를 URL 파라미터로 추가하여 원래 redirect_uri로 리다이렉트
    String redirectUrl = redirectUri;
    if (redirectUrl.contains("?")) {
      redirectUrl += "&token=" + token + "&refresh_token=" + refreshToken;
    } else {
      redirectUrl += "?token=" + token + "&refresh_token=" + refreshToken;
    }

    log.debug("프론트엔드로 리다이렉트: {}", redirectUrl);
    response.sendRedirect(redirectUrl);
  }

  /** 로그인 페이지 대체용 임시 핸들러 로그인 페이지로 직접 접근 시 프론트엔드로 리다이렉트 */
  @GetMapping("/login-redirect")
  public void loginRedirect(HttpServletResponse response) throws Exception {
    log.info("로그인 페이지 리다이렉트: 프론트엔드 로그인 페이지로 이동");
    response.sendRedirect(frontendDomain + "/login");
  }
}
