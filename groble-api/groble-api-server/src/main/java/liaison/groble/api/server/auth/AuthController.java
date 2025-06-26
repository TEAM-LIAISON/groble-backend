package liaison.groble.api.server.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.api.model.auth.response.SignInTestResponse;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.mapping.auth.AuthMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 인증 관련 API 컨트롤러 회원가입, 로그인, 이메일 인증, 토큰 갱신 등의 엔드포인트 제공 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "[⚠️ 로그아웃, 회원탈퇴, 테스트용 로그인]", description = "로그아웃, 회원탈퇴, 테스트용 로그인 API")
public class AuthController {

  // API 경로 상수화
  private static final String DEPRECATED_SIGN_IN_PATH = "/sign-in";
  private static final String DEPRECATED_SIGN_IN_TEST_PATH = "/sign-in/local/test";
  private static final String LOGOUT = "/logout";
  private static final String WITHDRAWAL = "/withdrawal";

  // 응답 메시지 상수화
  private static final String DEPRECATED_SIGN_IN_SUCCESS_MESSAGE =
      "[Deprecated 예정] 통합 계정으로 로그인이 성공적으로 완료되었습니다.";
  private static final String DEPRECATED_SIGN_IN_TEST_SUCCESS_MESSAGE =
      "[Deprecated 예정] 테스트용 통합 계정 로그인이 성공적으로 완료되었습니다.";
  private static final String LOGOUT_SUCCESS_MESSAGE = "로그아웃이 성공적으로 처리되었습니다.";
  private static final String WITHDRAWAL_SUCCESS_MESSAGE = "회원탈퇴가 성공적으로 처리되었습니다.";

  private final AuthService authService;
  private final AuthMapper authMapper;
  private final TokenCookieService tokenCookieService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[🛠️ Deprecated 예정] 통합 계정 로그인",
      description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다.")
  @PostMapping(DEPRECATED_SIGN_IN_PATH)
  public ResponseEntity<GrobleResponse<SignInResponse>> signIn(
      @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody
          SignInRequest signInRequest,
      HttpServletResponse response) {
    SignInDTO signInDto = authMapper.toSignInDto(signInRequest);

    SignInAuthResultDTO signInAuthResultDTO = authService.signIn(signInDto);

    tokenCookieService.addTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    SignInResponse signInResponse =
        authMapper.toSignInResponse(signInRequest.getEmail(), signInAuthResultDTO);

    return responseHelper.success(
        signInResponse, DEPRECATED_SIGN_IN_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[🛠 Deprecated 예정] 테스트용 통합 계정 로그인",
      description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다.")
  @PostMapping(DEPRECATED_SIGN_IN_TEST_PATH)
  public ResponseEntity<GrobleResponse<SignInTestResponse>> signInTest(
      @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody SignInRequest request,
      HttpServletResponse response) {

    SignInDTO signInDto = authMapper.toSignInDto(request);
    SignInAuthResultDTO signInAuthResultDTO = authService.signIn(signInDto);

    tokenCookieService.addTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    SignInTestResponse signInTestResponse =
        authMapper.toSignInTestResponse(request.getEmail(), signInAuthResultDTO);
    return responseHelper.success(
        signInTestResponse, DEPRECATED_SIGN_IN_TEST_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(summary = "[🛠 로그아웃]", description = "로그아웃을 통해 쿠키와 토큰을 무효화합니다.")
  @PostMapping(LOGOUT)
  public ResponseEntity<GrobleResponse<Void>> logout(
      @Auth Accessor accessor, HttpServletRequest request, HttpServletResponse response) {
    tokenCookieService.clearTokenCookies(request, response);
    return responseHelper.success(null, LOGOUT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(summary = "[🛠 회원탈퇴]", description = "사용자 계정을 탈퇴 처리합니다.")
  @PostMapping(WITHDRAWAL)
  public ResponseEntity<GrobleResponse<Void>> withdrawUser(
      @Auth Accessor accessor,
      @Valid @RequestBody UserWithdrawalRequest request,
      HttpServletResponse response) {

    UserWithdrawalDTO dto = authMapper.toUserWithdrawalDto(request);
    authService.withdrawUser(accessor.getUserId(), dto);

    tokenCookieService.removeTokenCookies(response);

    return responseHelper.success(null, WITHDRAWAL_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
