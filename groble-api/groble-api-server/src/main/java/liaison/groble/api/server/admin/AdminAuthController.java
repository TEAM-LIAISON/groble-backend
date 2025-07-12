package liaison.groble.api.server.admin;

import static org.springframework.http.HttpStatus.OK;

import jakarta.servlet.http.HttpServletRequest;
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
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.TokenCookieService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
@Tag(
    name = "[✅ 관리자 인증/인가] 관리자의 로그인 및 로그아웃 기능",
    description = "관리자 권한을 부여 받은 계정으로 로그인 및 로그아웃을 처리하는 API입니다.")
public class AdminAuthController {

  // API 경로 상수화
  private static final String ADMIN_SIGN_IN_PATH = "/sign-in";
  private static final String ADMIN_LOGOUT_PATH = "/logout";

  // 응답 메시지 상수화
  private static final String ADMIN_SIGN_IN_SUCCESS_MESSAGE = "관리자 로그인에 성공했습니다.";
  private static final String ADMIN_LOGOUT_SUCCESS_MESSAGE = "관리자 로그아웃이 성공적으로 처리되었습니다.";

  // Service
  private final AdminAuthService adminAuthService;
  private final TokenCookieService tokenCookieService;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(summary = "[✅ 관리자 로그인] 관리자 계정으로 로그인")
  @PostMapping(ADMIN_SIGN_IN_PATH)
  @Logging(item = "AdminAuth", action = "adminSignIn")
  public ResponseEntity<GrobleResponse<AdminSignInResponse>> adminSignIn(
      @Parameter(description = "관리자 로그인 요청 객체", required = true) @Valid @RequestBody
          AdminSignInRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    SignInAuthResultDTO signInAuthResultDTO =
        adminAuthService.adminSignIn(request.getEmail(), request.getPassword());
    tokenCookieService.addAdminTokenCookies(
        httpRequest,
        httpResponse,
        signInAuthResultDTO.getAccessToken(),
        signInAuthResultDTO.getRefreshToken());

    AdminSignInResponse adminSignInResponse = AdminSignInResponse.of(request.getEmail(), "ADMIN");
    return responseHelper.success(adminSignInResponse, ADMIN_SIGN_IN_SUCCESS_MESSAGE, OK);
  }

  @Operation(summary = "[✅ 관리자 로그아웃] 관리자 계정 로그아웃")
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ADMIN_LOGOUT_PATH)
  @Logging(item = "AdminAuth", action = "adminLogout")
  public ResponseEntity<GrobleResponse<Void>> adminLogout(
      @Auth Accessor accessor, HttpServletResponse response) {
    tokenCookieService.removeAdminTokenCookies(response);

    return responseHelper.success(null, ADMIN_LOGOUT_SUCCESS_MESSAGE, OK);
  }
}
