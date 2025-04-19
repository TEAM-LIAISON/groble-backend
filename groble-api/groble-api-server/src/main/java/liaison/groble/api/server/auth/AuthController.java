package liaison.groble.api.server.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.auth.request.DeprecatedSignUpRequest;
import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.ResetPasswordRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.api.model.auth.response.SignUpResponse;
import liaison.groble.api.model.user.request.UserTypeRequest;
import liaison.groble.api.server.auth.mapper.AuthDtoMapper;
import liaison.groble.application.auth.dto.DeprecatedSignUpDto;
import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.utils.CookieUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 인증 관련 API 컨트롤러 회원가입, 로그인, 이메일 인증, 토큰 갱신 등의 엔드포인트 제공 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "통합 회원가입, 통합 로그인, 이메일 인증, 토큰 갱신 등의 인증 관련 API")
public class AuthController {
  private final AuthService authService;
  private final UserService userService;
  private final AuthDtoMapper authDtoMapper;

  // 쿠키 설정값
  private static final int ACCESS_TOKEN_MAX_AGE = 60 * 30; // 30분
  private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7; // 7일
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  @Value("${app.cookie.domain}")
  private String cookieDomain; // 쿠키 도메인 설정

  /**
   * 회원가입 API
   *
   * <p>유형 선택, 약관, 이메일, 비밀번호, 닉네임
   */
  @Operation(
      summary = "통합 회원가입 (유형, 약관, 이메일(인증된), 비밀번호, 닉네임)",
      description = "새로운 사용자를 등록하고 인증 토큰을 발급합니다.")
  @PostMapping("/sign-up")
  public ResponseEntity<GrobleResponse<SignUpResponse>> signUp(
      @Parameter(description = "회원가입 정보", required = true) @Valid @RequestBody
          SignUpRequest request,
      HttpServletResponse response) {

    // 1. API DTO → 서비스 DTO 변환
    SignUpDto signUpDto = authDtoMapper.toServiceSignUpDto(request);

    // 2. 서비스 호출
    TokenDto tokenDto = authService.signUp(signUpDto);

    // 3. 토큰을 쿠키로 설정
    addTokenCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

    // 4. 사용자 정보만 응답 본문에 포함
    SignUpResponse signUpResponse = SignUpResponse.of(request.getEmail());

    // 5. API 응답 생성
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(GrobleResponse.success(signUpResponse, "회원가입이 성공적으로 완료되었습니다.", 201));
  }

  /**
   * 회원가입 API
   *
   * <p>이메일과 비밀번호로 회원가입 처리
   *
   * @param request 회원가입 요청 정보
   * @return 회원가입 결과 (액세스 토큰, 리프레시 토큰 : 쿠키에 세팅 | 해당 이메일에 대한 인증 완료 여부)
   */
  @Operation(summary = "통합 회원가입 [deprecated]", description = "새로운 사용자를 등록하고 인증 토큰을 발급합니다.")
  @PostMapping("/sign-up/deprecated")
  public ResponseEntity<GrobleResponse<SignUpResponse>> signUp(
      @Parameter(description = "회원가입 정보", required = true) @Valid @RequestBody
          DeprecatedSignUpRequest request,
      HttpServletResponse response) {

    // 1. API DTO → 서비스 DTO 변환
    DeprecatedSignUpDto deprecatedSignUpDto = authDtoMapper.toServiceDeprecatedSignUpDto(request);

    // 2. 서비스 호출
    TokenDto tokenDto = authService.signUp(deprecatedSignUpDto);

    // 3. 토큰을 쿠키로 설정
    addTokenCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

    // 4. 사용자 정보만 응답 본문에 포함
    SignUpResponse signUpResponse = SignUpResponse.of(request.getEmail());

    // 5. API 응답 생성
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(GrobleResponse.success(signUpResponse, "회원가입이 성공적으로 완료되었습니다.", 201));
  }

  /**
   * 로그인 API
   *
   * <p>이메일과 비밀번호로 로그인 처리
   *
   * @param request 로그인 요청 정보
   * @return 로그인 결과 (액세스 토큰, 리프레시 토큰 포함)
   */
  @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/sign-in")
  public ResponseEntity<GrobleResponse<SignInResponse>> signIn(
      @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody SignInRequest request,
      HttpServletResponse response) {
    log.info("로그인 요청: {}", request.getEmail());

    // 1. API DTO → 서비스 DTO 변환
    SignInDto signInDto = authDtoMapper.toServiceSignInDto(request);

    // 2. 서비스 호출
    TokenDto tokenDto = authService.signIn(signInDto);

    // 3. 사용자 역할 및 정보 상태 확인
    String userType = userService.getUserType(signInDto.getEmail());

    // 4. 사용자 라우팅 경로 설정
    String nextRoutePath = userService.getNextRoutePath(signInDto.getEmail());

    // 5. 토큰을 쿠키로 설정
    addTokenCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

    // 6. 사용자 정보와 역할 정보를 응답 본문에 포함
    SignInResponse signInResponse = SignInResponse.of(request.getEmail(), userType, nextRoutePath);

    // 7. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(signInResponse, "로그인이 성공적으로 완료되었습니다.", 200));
  }

  /** 로그아웃 API - 토큰 무효화 및 쿠키 삭제 */
  @PostMapping("/logout")
  @Operation(summary = "로그아웃", description = "로그아웃을 통해 쿠키와 토큰을 무효화합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그아웃 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  public ResponseEntity<GrobleResponse<Void>> logout(
      @Auth Accessor accessor, HttpServletRequest request, HttpServletResponse response) {

    log.info("로그아웃 요청: {}", accessor.getEmail());

    try {
      // 1. 리프레시 토큰 무효화
      authService.logout(accessor.getUserId());

      // 2. 쿠키 삭제
      CookieUtils.deleteCookie(request, response, ACCESS_TOKEN_COOKIE_NAME);
      CookieUtils.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);

      // 3. 응답 반환
      return ResponseEntity.ok().body(GrobleResponse.success(null, "로그아웃이 성공적으로 처리되었습니다.", 200));

    } catch (Exception e) {
      log.error("로그아웃 처리 중 오류 발생", e);
      return ResponseEntity.internalServerError()
          .body(GrobleResponse.error("로그아웃 처리 중 오류가 발생했습니다.", 500));
    }
  }

  /**
   * 비밀번호 재설정 이메일 발송 API
   *
   * @param request 비밀번호 재설정 이메일 요청
   * @return 이메일 발송 결과
   */
  @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 링크가 포함된 이메일을 발송합니다.")
  @PostMapping("/password/reset-request")
  public ResponseEntity<GrobleResponse<Void>> requestPasswordReset(
      @Valid @RequestBody EmailVerificationRequest request) {
    log.info("비밀번호 재설정 이메일 요청: {}", request.getEmail());

    authService.sendPasswordResetEmail(request.getEmail());

    return ResponseEntity.ok().body(GrobleResponse.success(null, "비밀번호 재설정 이메일이 발송되었습니다.", 200));
  }

  /**
   * 비밀번호 재설정 API
   *
   * <p>비밀번호 재설정 링크를 통해 새로운 비밀번호로 변경
   *
   * @param accessor 인증된 사용자 정보
   * @param request 비밀번호 재설정 요청
   * @return 비밀번호 재설정 결과
   */
  @Operation(summary = "비밀번호 재설정", description = "새로운 비밀번호로 재설정합니다.")
  @PostMapping("/password/reset")
  public ResponseEntity<GrobleResponse<Void>> resetPassword(
      @Auth Accessor accessor, @Valid @RequestBody ResetPasswordRequest request) {

    authService.resetPassword(accessor.getUserId(), request.getToken(), request.getNewPassword());

    return ResponseEntity.ok().body(GrobleResponse.success(null, "비밀번호가 성공적으로 재설정되었습니다.", 200));
  }

  @Operation(summary = "통합 회원가입 이메일 인증 요청", description = "사용자가 기입한 이메일에 인증 코드를 발급합니다.")
  @PostMapping("/email-verification/sign-up")
  public ResponseEntity<GrobleResponse<Void>> sendEmailVerificationForSignUp(
      @Parameter(description = "이메일 인증 정보", required = true) @Valid @RequestBody
          EmailVerificationRequest request) {
    log.info("이메일 인증 요청: {}", request.getEmail());

    // 1. API DTO → 서비스 DTO 변환
    EmailVerificationDto emailVerificationDto =
        authDtoMapper.toServiceEmailVerificationDto(request);

    // 2. 서비스 호출
    authService.sendEmailVerificationForSignUp(emailVerificationDto);

    // 3. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(null, "인증 이메일이 발송되었습니다.", 200));
  }

  @Operation(summary = "이메일 변경 이메일 인증 요청", description = "사용자가 기입한 이메일에 인증 코드를 발급합니다.")
  @PostMapping("/email-verification/change-email")
  public ResponseEntity<GrobleResponse<Void>> sendEmailVerificationForChangeEmail(
      @Auth Accessor accessor,
      @Parameter(description = "이메일 인증 정보", required = true) @Valid @RequestBody
          EmailVerificationRequest request) {
    log.info("이메일 변경 인증 요청: {}", request.getEmail());

    // 1. API DTO → 서비스 DTO 변환
    EmailVerificationDto emailVerificationDto =
        authDtoMapper.toServiceEmailVerificationDto(request);

    // 2. 서비스 호출
    authService.sendEmailVerificationForChangeEmail(accessor.getUserId(), emailVerificationDto);

    // 3. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(null, "인증 이메일이 발송되었습니다.", 200));
  }

  @Operation(summary = "회원가입 시 이메일 인증 코드 확인", description = "이메일로 발송된 인증 코드의 유효성을 검증합니다.")
  @PostMapping("/verify-code/sign-up")
  public ResponseEntity<GrobleResponse<Void>> verifyEmailCode(
      @Valid @RequestBody VerifyEmailCodeRequest request) {
    log.info("이메일 인증 코드 검증 요청: {}", request.getEmail());

    // API DTO → 서비스 DTO 변환
    VerifyEmailCodeDto verifyEmailCodeDto = authDtoMapper.toServiceVerifyEmailCodeDto(request);

    authService.verifyEmailCode(verifyEmailCodeDto);

    return ResponseEntity.ok().body(GrobleResponse.success(null, "이메일 인증이 성공적으로 완료되었습니다.", 200));
  }

  @Operation(
      summary = "이메일 변경 시 이메일 인증 코드 확인",
      description = "이메일 변경 시 인증 코드의 유효성을 검증하고 이메일을 변경합니다.")
  @PostMapping("/verify-code/change-email")
  public ResponseEntity<GrobleResponse<Void>> verifyEmailCodeForChangeEmail(
      @Auth Accessor accessor, @Valid @RequestBody VerifyEmailCodeRequest request) {
    log.info("이메일 변경 인증 코드 검증 요청: {}", request.getEmail());

    // API DTO → 서비스 DTO 변환
    VerifyEmailCodeDto verifyEmailCodeDto = authDtoMapper.toServiceVerifyEmailCodeDto(request);

    // 서비스 호출
    authService.verifyEmailCodeForChangeEmail(accessor.getUserId(), verifyEmailCodeDto);

    // API 응답 생성
    return ResponseEntity.ok().body(GrobleResponse.success(null, "이메일 변경 인증이 성공적으로 완료되었습니다.", 200));
  }

  /** 토큰 검증 및 로그인 상태 확인 API OAuth2 로그인 처리 후 프론트엔드에서 호출하여 토큰 상태 확인 */
  @Operation(summary = "토큰 검증", description = "현재 사용자의 인증 토큰을 검증하고 로그인 상태를 확인합니다.")
  @PostMapping("/validate-token")
  public ResponseEntity<GrobleResponse<SignInResponse>> validateToken(@Auth Accessor accessor) {
    // 사용자 역할 및 정보 상태 확인
    String userType = userService.getUserType(accessor.getEmail());

    // 사용자 라우팅 경로 설정
    String nextRoutePath = userService.getNextRoutePath(accessor.getEmail());

    // 사용자 정보 응답
    SignInResponse response = SignInResponse.of(accessor.getEmail(), userType, nextRoutePath);

    return ResponseEntity.ok().body(GrobleResponse.success(response, "유효한 토큰입니다.", 200));
  }

  @Operation(summary = "accessToken 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
  @PostMapping("/refresh-token")
  public ResponseEntity<GrobleResponse<Void>> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {

    TokenDto newTokens = authService.refreshTokens(extractRefreshTokenFromCookie(request));

    addTokenCookies(response, newTokens.getAccessToken(), newTokens.getRefreshToken());

    return ResponseEntity.ok(GrobleResponse.success(null, "토큰이 재발급되었습니다.", 200));
  }

  // 처음 회원가입 유형을 선택하는 API
  @Operation(summary = "회원가입 유형 선택", description = "회원가입 시 판매자 또는 구매자 중 선택합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "회원가입 유형 선택 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
  })
  @PostMapping("/initial-user-type")
  public ResponseEntity<GrobleResponse<Void>> setInitialUserType(
      @Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {

    userService.setInitialUserType(accessor.getUserId(), request.getUserType());
    return ResponseEntity.ok(GrobleResponse.success(null, "회원가입 유형이 설정되었습니다."));
  }

  /** 액세스 토큰과 리프레시 토큰을 쿠키에 저장 */
  private void addTokenCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {
    // Access Token - HttpOnly 설정 (JS에서 접근 불가)
    CookieUtils.addCookie(
        response,
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        ACCESS_TOKEN_MAX_AGE,
        "/",
        true,
        isSecureEnvironment(),
        "None", // SameSite 설정을 None으로 변경 (크로스 사이트 요청 허용)
        cookieDomain); // 도메인 설정 추가

    // Refresh Token - HttpOnly 설정 (JS에서 접근 불가, 보안 강화)
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        isSecureEnvironment(),
        "None", // SameSite 설정을 None으로 변경
        cookieDomain); // 도메인 설정 추가

    log.debug(
        "토큰 쿠키 추가 완료: domain={}, accessToken({}초), refreshToken({}초)",
        cookieDomain,
        ACCESS_TOKEN_MAX_AGE,
        REFRESH_TOKEN_MAX_AGE);
  }

  /** 보안 환경(운영)인지 확인 */
  private boolean isSecureEnvironment() {
    String env = System.getProperty("spring.profiles.active", "dev");
    return env.equalsIgnoreCase("prod") || env.equalsIgnoreCase("production");
  }

  private String extractRefreshTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      throw new IllegalArgumentException("쿠키가 없습니다.");
    }

    for (Cookie cookie : request.getCookies()) {
      if ("refreshToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }

    throw new IllegalArgumentException("refreshToken 쿠키가 없습니다.");
  }
}
