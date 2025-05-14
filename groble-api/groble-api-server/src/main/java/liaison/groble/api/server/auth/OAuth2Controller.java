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

  @Value("${app.cookie.domain}")
  private String cookieDomain;

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

    // 환경에 따른 설정 결정
    String activeProfile = System.getProperty("spring.profiles.active", "local");
    boolean isLocal = activeProfile.contains("local") || activeProfile.isEmpty();

    // 개발/운영 환경에서는 HTTPS 사용하므로 Secure=true
    boolean isSecure = !isLocal;

    // OAuth2 리다이렉트 흐름을 위해 SameSite=None 설정
    // SameSite=None일 때는 항상 Secure=true 설정 (브라우저 요구사항)
    String sameSite = "None";
    if (sameSite.equals("None")) {
      isSecure = true;
    }

    // 도메인 설정: 로컬 환경에서는 설정하지 않음
    String domain = null;
    if (!isLocal) {
      domain = cookieDomain; // app.cookie.domain 속성값 사용 (groble.im)
    }

    // 쿠키 세팅 - redirect URI를 HttpOnly=false로 설정 (JS에서 읽을 수 있도록)
    CookieUtils.addCookie(
        response,
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
        redirectUri,
        180, // 3분 유효
        "/", // path
        false, // httpOnly = false (JS에서 읽을 수 있도록)
        isSecure, // secure
        sameSite, // sameSite
        domain); // domain

    log.debug(
        "리다이렉트 URI 쿠키 설정: {}, domain={}, secure={}, sameSite={}",
        redirectUri,
        domain != null ? domain : "기본값",
        isSecure,
        sameSite);

    // Redirect to OAuth2 provider
    response.sendRedirect("/oauth2/authorize/" + provider);
  }
}
