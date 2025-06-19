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

    // 요청 출처 확인을 위한 헤더 정보 로깅
    String origin = request.getHeader("Origin");
    String referer = request.getHeader("Referer");
    String host = request.getHeader("Host");

    log.info("OAuth2 요청 정보 - Origin: {}, Referer: {}, Host: {}", origin, referer, host);

    // 요청 출처에 따라 프론트엔드 도메인 동적 설정
    String actualFrontendDomain = determineFrontendDomain(origin, referer);
    log.info("결정된 프론트엔드 도메인: {}", actualFrontendDomain);

    // 상대 경로인 경우 프론트엔드 도메인을 앞에 추가
    if (redirectUri.startsWith("/")) {
      redirectUri = actualFrontendDomain + redirectUri;
    }

    log.info(
        "OAuth2 로그인 시작: provider={}, redirect_uri={}, frontend_domain={}",
        provider,
        redirectUri,
        actualFrontendDomain);

    // 환경에 따른 설정 결정
    String activeProfile = System.getProperty("spring.profiles.active", "local");
    boolean isLocal = activeProfile.contains("local") || activeProfile.isEmpty();

    // 요청 출처에 따른 추가 설정
    boolean isFromLocalhost = actualFrontendDomain.contains("localhost");

    // 개발/운영 환경에서는 HTTPS 사용하므로 Secure=true
    // localhost에서는 http이므로 Secure=false
    boolean isSecure = !isLocal && !isFromLocalhost;

    // OAuth2 리다이렉트 흐름을 위해 SameSite=None 설정
    // SameSite=None일 때는 항상 Secure=true 설정 (브라우저 요구사항)
    String sameSite = "None";
    if (sameSite.equals("None") && !isFromLocalhost) {
      isSecure = true;
    }

    // 도메인 설정: 로컬 환경이거나 localhost에서의 요청인 경우 설정하지 않음
    String domain = null;
    if (!isLocal && !isFromLocalhost) {
      domain = cookieDomain; // app.cookie.domain 속성값 사용 (groble.im)
    }

    log.info(
        "쿠키 설정 - isFromLocalhost: {}, isLocal: {}, domain: {}, secure: {}, sameSite: {}",
        isFromLocalhost,
        isLocal,
        domain,
        isSecure,
        sameSite);

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

  /**
   * 요청 출처에 따라 프론트엔드 도메인을 결정하는 메서드
   *
   * @param origin Origin 헤더 값
   * @param referer Referer 헤더 값
   * @return 결정된 프론트엔드 도메인
   */
  private String determineFrontendDomain(String origin, String referer) {
    // Origin 헤더가 있으면 우선 사용
    if (origin != null && !origin.isEmpty()) {
      if (origin.contains("localhost:3000")) {
        log.debug("로컬호스트에서의 요청 감지: {}", origin);
        return "http://localhost:3000";
      } else if (origin.contains("dev.groble.im")) {
        log.debug("개발 환경에서의 요청 감지: {}", origin);
        return "https://dev.groble.im";
      } else if (origin.equals("https://groble.im") || origin.contains("://groble.im")) {
        log.debug("프로덕션 환경에서의 요청 감지: {}", origin);
        return "https://groble.im";
      }
    }

    // Origin이 없으면 Referer 헤더 확인
    if (referer != null && !referer.isEmpty()) {
      if (referer.contains("localhost:3000")) {
        log.debug("Referer를 통해 로컬호스트 요청 감지: {}", referer);
        return "http://localhost:3000";
      } else if (referer.contains("dev.groble.im")) {
        log.debug("Referer를 통해 개발 환경 요청 감지: {}", referer);
        return "https://dev.groble.im";
      } else if (referer.contains("://groble.im")) {
        log.debug("Referer를 통해 프로덕션 환경 요청 감지: {}", referer);
        return "https://groble.im";
      }
    }

    // 둘 다 없으면 기본값으로 frontendDomain 사용
    log.warn("Origin과 Referer 헤더가 모두 없습니다. 기본 프론트엔드 도메인 사용: {}", frontendDomain);
    return frontendDomain;
  }
}
