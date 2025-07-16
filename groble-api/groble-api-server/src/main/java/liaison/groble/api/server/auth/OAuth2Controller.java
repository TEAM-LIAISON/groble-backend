package liaison.groble.api.server.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** OAuth2 인증 관련 컨트롤러 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
@Tag(name = "[🔑 소셜 계정] 소셜 로그인을 진행하는 기능 API", description = "OAuth2를 활용하여 소셜 로그인을 진행합니다.")
public class OAuth2Controller {

  // API 경로 상수화
  private static final String AUTHORIZE_PATH = "/authorize";

  // 환경별 프론트엔드 도메인 설정
  @Value("${app.frontend-url}")
  private String frontendDomain; // 환경별로 설정 가능하도록 변경

  /** OAuth2 인증 페이지로 리다이렉트하기 전에 리다이렉트 URI를 세션에 저장 */
  @Operation(summary = "OAuth2 로그인 시작", description = "소셜 로그인 시작 전 리다이렉트 URI를 설정합니다.")
  @GetMapping(AUTHORIZE_PATH)
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

    // 세션에 redirect_uri 저장
    request.getSession().setAttribute("redirect_uri", redirectUri);
    log.info("세션에 redirect_uri 저장 완료: {}", redirectUri);

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
