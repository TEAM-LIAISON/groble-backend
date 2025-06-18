package liaison.groble.api.server.admin;

import jakarta.servlet.http.HttpServletRequest;
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
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
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

  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  @Value("${app.cookie.admin-domain}")
  private String adminCookieDomain; // 쿠키 도메인 설정

  private final AdminAuthService adminAuthService;
  private final TokenCookieService tokenCookieService;

  @PostMapping("/sign-in")
  public ResponseEntity<GrobleResponse<AdminSignInResponse>> adminSignIn(
      @Parameter(description = "관리자 로그인 ", required = true) @Valid @RequestBody
          AdminSignInRequest request,
      HttpServletResponse response) {
    log.info("관리자 로그인 요청: {}", request.getEmail());
    SignInAuthResultDTO signInAuthResultDTO =
        adminAuthService.adminSignIn(request.getEmail(), request.getPassword());
    tokenCookieService.addAdminTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    AdminSignInResponse adminSignInResponse = AdminSignInResponse.of(request.getEmail(), "ADMIN");
    return ResponseEntity.ok(GrobleResponse.success(adminSignInResponse));
  }

  @RequireRole("ROLE_ADMIN")
  @PostMapping("/logout")
  public ResponseEntity<GrobleResponse<Void>> adminLogout(
      @Auth Accessor accessor, HttpServletRequest request, HttpServletResponse response) {
    try {
      // 2. 쿠키 삭제 - now with domain specification
      CookieUtils.deleteCookieWithDomain(
          request, response, ACCESS_TOKEN_COOKIE_NAME, adminCookieDomain);
      CookieUtils.deleteCookieWithDomain(
          request, response, REFRESH_TOKEN_COOKIE_NAME, adminCookieDomain);

      // 3. 응답 반환
      return ResponseEntity.ok().body(GrobleResponse.success(null, "로그아웃이 성공적으로 처리되었습니다.", 200));

    } catch (Exception e) {
      log.error("로그아웃 처리 중 오류 발생", e);
      return ResponseEntity.internalServerError()
          .body(GrobleResponse.error("로그아웃 처리 중 오류가 발생했습니다.", 500));
    }
  }
}
