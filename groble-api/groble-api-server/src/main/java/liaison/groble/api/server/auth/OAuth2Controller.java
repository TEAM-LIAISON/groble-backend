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
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 관련 컨트롤러 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OAuth2", description = "OAuth2 소셜 로그인 관련 API")
public class OAuth2Controller {

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
    // 쿠키 세팅
    CookieUtils.addCookie(
        response,
        redirectUri,
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
        180);

    // Redirect to OAuth2 provider
    response.sendRedirect("/oauth2/authorize/" + provider);
  }
}
