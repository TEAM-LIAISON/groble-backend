package liaison.groble.api.server.admin;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.request.AdminSignInRequest;
import liaison.groble.api.model.admin.response.AdminSignInResponse;
import liaison.groble.application.admin.service.AdminAuthService;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.utils.CookieUtils;
import liaison.groble.common.utils.TokenCookieService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "관리자의 로그인 기능 관련 API", description = "관리자 로그인 기능 API")
public class AdminAuthController {

  private final AdminAuthService adminAuthService;
  private final TokenCookieService tokenCookieService;

  private static final int ACCESS_TOKEN_MAX_AGE = 60 * 60; // 1시간
  private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7; // 1주일
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  @Value("${app.cookie.admin-domain}")
  private String adminCookieDomain; // 쿠키 도메인 설정

  @PostMapping("/sign-in")
  public ResponseEntity<GrobleResponse<AdminSignInResponse>> adminSignIn(
      @Parameter(description = "관리자 로그인 ", required = true) @Valid @RequestBody
          AdminSignInRequest request,
      HttpServletResponse response) {
    log.info("관리자 로그인 요청: {}", request.getEmail());
    SignInAuthResultDTO signInAuthResultDTO =
        adminAuthService.adminSignIn(request.getEmail(), request.getPassword());
    addTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    AdminSignInResponse adminSignInResponse = AdminSignInResponse.of(request.getEmail(), "ADMIN");
    return ResponseEntity.ok(GrobleResponse.success(adminSignInResponse));
  }

  private void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {

    // 1. 환경에 따른 설정 결정
    String activeProfile = System.getProperty("spring.profiles.active", "local");
    boolean isLocal = activeProfile.contains("local") || activeProfile.isEmpty();

    // 2. 로컬 환경이 아닌 경우에만 Secure 설정
    // 개발(dev) 및 운영(prod) 환경에서는 HTTPS 사용
    boolean isSecure = !isLocal;

    // 3. SameSite 설정: 크로스 사이트 요청을 허용하기 위해 'None' 사용
    // SameSite=None이면 항상 Secure=true여야 함 (브라우저 요구사항)
    String sameSite = "None";
    isSecure = true; // SameSite=None인 경우 항상 Secure 설정

    // 무조건 adminCookieDomain 사용
    String domain =
        adminCookieDomain != null && !adminCookieDomain.isEmpty()
            ? (adminCookieDomain.startsWith(".")
                ? adminCookieDomain.substring(1)
                : adminCookieDomain)
            : null;

    // Access Token
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/", // path
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
        "/", // path
        true, // httpOnly
        isSecure, // secure
        sameSite, // sameSite
        domain); // domain

    log.debug(
        "토큰 쿠키 추가 완료: env={}, domain={}, accessToken({}초), refreshToken({}초), secure={}, sameSite={}",
        activeProfile,
        domain != null ? domain : "기본값(localhost)",
        ACCESS_TOKEN_MAX_AGE,
        REFRESH_TOKEN_MAX_AGE,
        isSecure,
        sameSite);
  }
}
