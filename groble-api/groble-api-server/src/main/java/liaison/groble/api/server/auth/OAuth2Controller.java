package liaison.groble.api.server.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

  // 프론트엔드 도메인 설정
  private final String frontendDomain = "https://dev.groble.im";

  /** OAuth2 인증 페이지로 리다이렉트하기 전에 리다이렉트 URI를 쿠키에 저장 */
  @Operation(summary = "OAuth2 로그인 시작", description = "소셜 로그인 시작 전 리다이렉트 URI를 설정합니다.")
  @GetMapping("/authorize")
  public void authorize(
      @RequestParam("redirect_uri") String redirectUri,
      @RequestParam("provider") String provider,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {

    log.info("OAuth2 로그인 시작: provider={}, redirect_uri={}", provider, redirectUri);
    // 쿠키 세팅 - 수정: redirectUri를 쿠키 이름이 아닌 값으로 사용
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

    // 프론트엔드 응용 프로그램으로 리다이렉트
    // 토큰이 쿠키에 저장되어 있으므로 프론트엔드에서 쿠키를 읽을 수 있음
    String redirectUrl = frontendDomain + "/auth/login/success";
    response.sendRedirect(redirectUrl);
  }
}
