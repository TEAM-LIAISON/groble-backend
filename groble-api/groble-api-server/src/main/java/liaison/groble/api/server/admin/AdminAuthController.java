package liaison.groble.api.server.admin;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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
}
