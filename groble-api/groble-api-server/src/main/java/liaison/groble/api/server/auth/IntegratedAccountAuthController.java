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
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.api.model.auth.response.SignUpResponse;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.SignUpAuthResultDTO;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.service.IntegratedAccountAuthService;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.mapping.auth.AuthMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/integrated")
@Tag(name = "[통합 계정] 통합 계정의 회원가입 및 로그인 API", description = "통합 계정 회원가입, 로그인을 통해 토큰을 발급받습니다.")
public class IntegratedAccountAuthController {

  // API 경로 상수화
  private static final String SIGN_IN_PATH = "/sign-in";
  private static final String SIGN_UP_PATH = "/sign-up";

  // 응답 메시지 상수화
  private static final String SIGN_IN_SUCCESS_MESSAGE = "통합 계정으로 로그인이 성공적으로 완료되었습니다.";
  private static final String SIGN_UP_SUCCESS_MESSAGE = "회원가입이 성공적으로 완료되었습니다.";

  // 로그 메시지 상수화
  private static final String SIGN_IN_REQUEST_LOG = "통합 로그인 요청: {}";
  private static final String SIGN_UP_REQUEST_LOG = "통합 계정 회원가입 요청: {}";

  private final AuthMapper authMapper;
  private final IntegratedAccountAuthService integratedAccountAuthService;
  private final TokenCookieService tokenCookieService;

  @Operation(summary = "통합 계정 로그인", description = "이메일과 비밀번호로 통합 계정 로그인을 수행하고 인증 토큰을 발급합니다.")
  @PostMapping(SIGN_IN_PATH)
  public ResponseEntity<GrobleResponse<SignInResponse>> signIn(
      @Parameter(description = "로그인 정보 (이메일, 비밀번호)", required = true) @Valid @RequestBody
          SignInRequest request,
      HttpServletRequest httpServletRequest,
      HttpServletResponse response) {

    log.info(SIGN_IN_REQUEST_LOG, request.getEmail());

    // 로그인 처리
    SignInDTO signInDto = authMapper.toSignInDto(request);
    SignInAuthResultDTO authResult =
        integratedAccountAuthService.integratedAccountSignIn(signInDto);

    // 응답 생성
    SignInResponse signInResponse = authMapper.toSignInResponse(signInDto.getEmail(), authResult);

    // 토큰 쿠키 설정
    addTokenCookies(
        httpServletRequest, response, authResult.getAccessToken(), authResult.getRefreshToken());

    return createSuccessResponse(signInResponse, SIGN_IN_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(summary = "통합 계정 회원가입", description = "통합 계정으로 회원가입을 수행하고 인증 토큰을 발급합니다.")
  @PostMapping(SIGN_UP_PATH)
  public ResponseEntity<GrobleResponse<SignUpResponse>> signUp(
      @Parameter(description = "회원가입 정보 (이메일, 비밀번호, 사용자 타입, 약관 동의 등)", required = true)
          @Valid
          @RequestBody
          SignUpRequest request,
      HttpServletRequest httpServletRequest,
      HttpServletResponse response) {

    log.info(SIGN_UP_REQUEST_LOG, request.getEmail());

    // 회원가입 처리
    SignUpDto signUpDto = authMapper.toSignUpDto(request);
    SignUpAuthResultDTO authResult =
        integratedAccountAuthService.integratedAccountSignUp(signUpDto);

    // 응답 생성
    SignUpResponse signUpResponse = SignUpResponse.of(request.getEmail());

    // 토큰 쿠키 설정
    addTokenCookies(
        httpServletRequest, response, authResult.getAccessToken(), authResult.getRefreshToken());

    return createSuccessResponse(signUpResponse, SIGN_UP_SUCCESS_MESSAGE, HttpStatus.CREATED);
  }

  /** 토큰 쿠키 추가 헬퍼 메서드 */
  private void addTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken) {
    tokenCookieService.addTokenCookies(request, response, accessToken, refreshToken);
  }

  /** 성공 응답 생성 헬퍼 메서드 */
  private <T> ResponseEntity<GrobleResponse<T>> createSuccessResponse(
      T data, String message, HttpStatus status) {
    return ResponseEntity.status(status).body(GrobleResponse.success(data, message));
  }
}
