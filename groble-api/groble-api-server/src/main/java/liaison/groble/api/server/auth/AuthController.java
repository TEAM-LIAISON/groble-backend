package liaison.groble.api.server.auth;

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
import liaison.groble.api.server.auth.mapper.AuthDtoMapper;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.common.response.ApiResponse;
import liaison.groble.common.utils.CookieUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 인증 관련 API 컨트롤러 회원가입, 로그인, 이메일 인증, 토큰 갱신 등의 엔드포인트 제공 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;
  private final AuthDtoMapper mapper;

  // 쿠키 설정값
  private static final int ACCESS_TOKEN_MAX_AGE = 60 * 30; // 30분
  private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7; // 7일
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  /** 회원가입 API */
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
      @Valid @RequestBody SignUpRequest request, HttpServletResponse response) {

    log.info("회원가입 요청: {}", request.getEmail());

    // 1. API DTO → 서비스 DTO 변환
    SignUpDto signUpDto = mapper.toServiceSignUpDto(request);

    // 2. 서비스 호출
    TokenDto tokenDto = authService.signUp(signUpDto);

    // 3. 토큰을 쿠키로 설정
    addTokenCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

    // 4. 사용자 정보만 응답 본문에 포함
    SignUpResponse signUpResponse = SignUpResponse.of(request.getEmail());

    // 5. API 응답 생성
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(signUpResponse, "회원가입이 성공적으로 완료되었습니다.", 201));
  }

  /**
   * 로그인 API 이메일과 비밀번호로 로그인 처리
   *
   * @param request 로그인 요청 정보
   * @return 로그인 결과 (액세스 토큰, 리프레시 토큰 포함)
   */
  @PostMapping("/sign-in")
  public ResponseEntity<ApiResponse<SignInResponse>> signIn(
      @Valid @RequestBody SignInRequest request, HttpServletResponse response) {
    log.info("로그인 요청: {}", request.getEmail());

    // 1. API DTO → 서비스 DTO 변환
    SignInDto signInDto = mapper.toServiceSignInDto(request);

    // 2. 서비스 호출
    TokenDto tokenDto = authService.signIn(signInDto);

    // 3. 토큰을 쿠키로 설정
    addTokenCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

    // 4. 사용자 정보만 응답 본문에 포함
    SignInResponse signInResponse = SignInResponse.of(request.getEmail());

    // 5. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(signInResponse, "로그인이 성공적으로 완료되었습니다.", 200));
  }

  //  @PostMapping("/login")
  //  public ResponseEntity<?> login(@Valid @RequestBody SignInRequest request) {
  //    try {
  //      TokenResponse tokenResponse = authService.login(request);
  //
  //      return ResponseEntity.ok().body(Map.of("message", "로그인에 성공했습니다.", "token",
  // tokenResponse));
  //    } catch (AuthenticationFailedException e) {
  //      log.warn("로그인 실패: {}", request.getEmail());
  //      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
  //    } catch (Exception e) {
  //      log.error("로그인 처리 중 오류 발생", e);
  //      return ResponseEntity.badRequest().body(Map.of("message", "이메일 또는 비밀번호가 일치하지 않습니다."));
  //    }
  //  }
  //
  //  /**
  //   * 로그아웃 API 리프레시 토큰 무효화
  //   *
  //   * @param userDetails 인증된 사용자 정보
  //   * @return 로그아웃 결과
  //   */
  //  @PostMapping("/logout")
  //  @PreAuthorize("isAuthenticated()")
  //  public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
  //    try {
  //      authService.logout(userDetails.getId());
  //      return ResponseEntity.ok().body(Map.of("message", "로그아웃되었습니다."));
  //    } catch (Exception e) {
  //      log.error("로그아웃 처리 중 오류 발생", e);
  //      return ResponseEntity.internalServerError().body(Map.of("message", "로그아웃 처리 중 오류가
  // 발생했습니다."));
  //    }
  //  }
  //
  //  /**
  //   * 토큰 갱신 API 리프레시 토큰을 사용해 새 액세스 토큰 발급
  //   *
  //   * @param request 토큰 갱신 요청 정보
  //   * @return 갱신된 토큰 정보
  //   */
  //  @PostMapping("/token/refresh")
  //  public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
  //    try {
  //      TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
  //
  //      return ResponseEntity.ok().body(Map.of("message", "토큰이 갱신되었습니다.", "token",
  // tokenResponse));
  //    } catch (AuthenticationFailedException e) {
  //      log.warn("토큰 갱신 실패: {}", e.getMessage());
  //      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
  //    } catch (Exception e) {
  //      log.error("토큰 갱신 중 오류 발생", e);
  //      return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 리프레시 토큰입니다."));
  //    }
  //  }
  //
  //  /**
  //   * 비밀번호 변경 API 인증된 사용자가 비밀번호 변경
  //   *
  //   * @param request 비밀번호 변경 요청 정보
  //   * @param userDetails 인증된 사용자 정보
  //   * @return 비밀번호 변경 결과
  //   */
  //  @PostMapping("/password/change")
  //  @PreAuthorize("isAuthenticated()")
  //  public ResponseEntity<?> changePassword(
  //      @Valid @RequestBody ChangePasswordRequest request,
  //      @AuthenticationPrincipal UserDetailsImpl userDetails) {
  //
  //    try {
  //      boolean success =
  //          authService.changePassword(
  //              userDetails.getId(), request.getCurrentPassword(), request.getNewPassword());
  //
  //      if (success) {
  //        return ResponseEntity.ok().body(Map.of("message", "비밀번호가 변경되었습니다."));
  //      } else {
  //        return ResponseEntity.badRequest().body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
  //      }
  //    } catch (Exception e) {
  //      log.error("비밀번호 변경 중 오류 발생", e);
  //      return ResponseEntity.internalServerError().body(Map.of("message", "비밀번호 변경 중 오류가
  // 발생했습니다."));
  //    }
  //  }
  //
  //  /**
  //   * 비밀번호 재설정 API 이메일 인증 후 비밀번호 재설정 (비밀번호 분실 시 사용)
  //   *
  //   * @param request 비밀번호 재설정 요청 정보
  //   * @return 비밀번호 재설정 결과
  //   */
  //  @PostMapping("/password/reset")
  //  public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
  //    try {
  //      boolean success = authService.resetPassword(request.getEmail(), request.getNewPassword());
  //
  //      if (success) {
  //        return ResponseEntity.ok().body(Map.of("message", "비밀번호가 재설정되었습니다."));
  //      } else {
  //        return ResponseEntity.badRequest().body(Map.of("message", "비밀번호 재설정에 실패했습니다."));
  //      }
  //    } catch (EmailNotVerifiedException e) {
  //      log.warn("비밀번호 재설정 실패 - 이메일 미인증: {}", request.getEmail());
  //      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "verified",
  // false));
  //    } catch (Exception e) {
  //      log.error("비밀번호 재설정 중 오류 발생", e);
  //      return ResponseEntity.internalServerError().body(Map.of("message", "비밀번호 재설정 중 오류가
  // 발생했습니다."));
  //    }
  //  }
  //

  //  public ResponseEntity<?> requestVerification(
  //      @Valid @RequestBody AuthDTO.EmailVerificationRequest request) throws MessagingException {
  //    try {
  //      // 인증 이메일 발송
  //      String verificationId = emailVerificationService.sendVerificationEmail(request);
  //
  //      return ResponseEntity.ok()
  //          .body(
  //              Map.of(
  //                  "message",
  //                  "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.",
  //                  "email",
  //                  request.getEmail(),
  //                  "verificationId",
  //                  verificationId));
  //    } catch (EmailAlreadyExistsException e) {
  //      log.warn("이메일 인증 요청 실패 - 이미 가입된 이메일: {}", request.getEmail());
  //      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
  //    } catch (Exception e) {
  //      log.error("이메일 발송 실패", e);
  //      return ResponseEntity.internalServerError().body(Map.of("message", "이메일 발송 중 오류가
  // 발생했습니다."));
  //    }
  //  }
  //
  //  /**
  //   * 이메일 인증 상태 확인 API 프론트엔드에서 인증 상태 폴링에 사용
  //   *
  //   * @param email 확인할 이메일
  //   * @return 인증 상태 정보
  //   */
  //  @GetMapping("/email/verification-status")
  //  public ResponseEntity<?> checkVerificationStatus(@RequestParam("email") String email) {
  //    boolean verified = emailVerificationService.isEmailVerified(email);
  //    return ResponseEntity.ok().body(Map.of("verified", verified));
  //  }
  //
  //  /**
  //   * 이메일 인증 처리 API 이메일의 인증 링크 클릭 시 호출됨
  //   *
  //   * @param token 인증 토큰
  //   * @param encodedEmail 인코딩된 이메일
  //   * @return 인증 결과 페이지로 리다이렉트
  //   */
  //  @GetMapping("/verify")
  //  public RedirectView verifyEmail(
  //      @RequestParam("token") String token, @RequestParam("email") String encodedEmail) {
  //
  //    try {
  //      // 이메일 인증 처리
  //      boolean verified = emailVerificationService.verifyEmail(token);
  //
  //      if (verified) {
  //        // 인증 성공 시 성공 페이지로 리다이렉트
  //        return new RedirectView("/verification-success.html?email=" + encodedEmail);
  //      } else {
  //        // 인증 실패 시 실패 페이지로 리다이렉트
  //        return new RedirectView("/verification-failed.html");
  //      }
  //    } catch (InvalidTokenException e) {
  //      // 유효하지 않은 토큰
  //      return new RedirectView("/verification-failed.html?error=" + e.getMessage());
  //    } catch (Exception e) {
  //      // 기타 오류
  //      log.error("이메일 인증 처리 중 오류 발생", e);
  //      return new RedirectView("/verification-failed.html?error=" + e.getMessage());
  //    }
  //  }

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
        "Lax");

    // Refresh Token - HttpOnly 설정 (JS에서 접근 불가, 보안 강화)
    CookieUtils.addCookie(
        response,
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        REFRESH_TOKEN_MAX_AGE,
        "/",
        true,
        isSecureEnvironment(),
        "Lax");

    log.debug(
        "토큰 쿠키 추가 완료: accessToken({}초), refreshToken({}초)",
        ACCESS_TOKEN_MAX_AGE,
        REFRESH_TOKEN_MAX_AGE);
  }

  /** 보안 환경(운영)인지 확인 */
  private boolean isSecureEnvironment() {
    String env = System.getProperty("spring.profiles.active", "dev");
    return env.equalsIgnoreCase("prod") || env.equalsIgnoreCase("production");
  }
}
