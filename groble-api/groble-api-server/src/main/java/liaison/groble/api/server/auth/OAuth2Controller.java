package liaison.groble.api.server.auth;

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
@Tag(name = "소셜 로그인", description = "OAuth2 소셜 로그인 API")
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
}
