package liaison.groble.api.server.admin;

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
import liaison.groble.api.server.admin.docs.AdminAuthSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
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

@RestController
@RequestMapping(ApiPaths.Admin.AUTH_BASE)
@Tag(name = AdminAuthSwaggerDocs.TAG_NAME, description = AdminAuthSwaggerDocs.TAG_DESCRIPTION)
public class AdminAuthController extends BaseController {

  private final AdminAuthService adminAuthService;
  private final TokenCookieService tokenCookieService;

  public AdminAuthController(
      ResponseHelper responseHelper,
      AdminAuthService adminAuthService,
      TokenCookieService tokenCookieService) {
    super(responseHelper);
    this.adminAuthService = adminAuthService;
    this.tokenCookieService = tokenCookieService;
  }

  @Operation(
      summary = AdminAuthSwaggerDocs.SIGN_IN_SUMMARY,
      description = AdminAuthSwaggerDocs.SIGN_IN_DESCRIPTION)
  @PostMapping(ApiPaths.Admin.AUTH_SIGN_IN)
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
    return success(adminSignInResponse, ResponseMessages.Admin.ADMIN_SIGN_IN_SUCCESS);
  }

  @Operation(
      summary = AdminAuthSwaggerDocs.LOGOUT_SUMMARY,
      description = AdminAuthSwaggerDocs.LOGOUT_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.AUTH_LOGOUT)
  @Logging(item = "AdminAuth", action = "adminLogout")
  public ResponseEntity<GrobleResponse<Void>> adminLogout(
      @Auth Accessor accessor, HttpServletResponse response) {
    tokenCookieService.removeAdminTokenCookies(response);

    return successVoid(ResponseMessages.Admin.ADMIN_LOGOUT_SUCCESS);
  }
}
