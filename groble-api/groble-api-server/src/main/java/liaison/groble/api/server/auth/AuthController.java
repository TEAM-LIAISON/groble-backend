package liaison.groble.api.server.auth;

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

import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.api.model.auth.response.SignInTestResponse;
import liaison.groble.api.server.auth.mapper.AuthDtoMapper;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.utils.CookieUtils;
import liaison.groble.mapping.auth.AuthMapper;

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
@Tag(name = "[⚠️ 로그아웃, 회원탈퇴, 테스트용 로그인]", description = "로그아웃, 회원탈퇴, 테스트용 로그인 API")
public class AuthController {
  private final AuthService authService;
  private final AuthDtoMapper authDtoMapper;
  private final AuthMapper authMapper;

  // 쿠키 설정값
  private static final int ACCESS_TOKEN_MAX_AGE = 60 * 60; // 1시간
  private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7; // 1주일
  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  @Value("${app.cookie.domain}")
  private String cookieDomain; // 쿠키 도메인 설정

  @Operation(summary = "[Deprecated 예정] 통합 계정 로그인", description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다.")
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

    SignInDTO signInDto = authDtoMapper.toServiceSignInDto(request);

    SignInAuthResultDTO signInAuthResultDTO = authService.signIn(signInDto);

    addTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    SignInResponse signInResponse =
        authMapper.toSignInResponse(request.getEmail(), signInAuthResultDTO);

    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(signInResponse, "로그인이 성공적으로 완료되었습니다.", 200));
  }

  @Operation(
      summary = "[Deprecated 예정] 테스트용 통합 계정 로그인",
      description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/sign-in/local/test")
  public ResponseEntity<GrobleResponse<SignInTestResponse>> signInTest(
      @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody SignInRequest request,
      HttpServletResponse response) {
    log.info("(localhost:3000) 포트에서 사용하는 테스트 로그인 요청: {}", request.getEmail());

    SignInDTO signInDto = authDtoMapper.toServiceSignInDto(request);
    SignInAuthResultDTO signInAuthResultDTO = authService.signIn(signInDto);

    addTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    // 6. 사용자 정보와 역할 정보를 응답 본문에 포함
    SignInTestResponse signInTestResponse =
        SignInTestResponse.of(
            request.getEmail(),
            signInAuthResultDTO.isHasAgreedToTerms(),
            signInAuthResultDTO.isHasNickname(),
            signInAuthResultDTO.getAccessToken(),
            signInAuthResultDTO.getRefreshToken());

    // 7. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(signInTestResponse, "로그인이 성공적으로 완료되었습니다.", 200));
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

      // 2. 쿠키 삭제 - now with domain specification
      CookieUtils.deleteCookieWithDomain(request, response, ACCESS_TOKEN_COOKIE_NAME, cookieDomain);
      CookieUtils.deleteCookieWithDomain(
          request, response, REFRESH_TOKEN_COOKIE_NAME, cookieDomain);

      // 3. 응답 반환
      return ResponseEntity.ok().body(GrobleResponse.success(null, "로그아웃이 성공적으로 처리되었습니다.", 200));

    } catch (Exception e) {
      log.error("로그아웃 처리 중 오류 발생", e);
      return ResponseEntity.internalServerError()
          .body(GrobleResponse.error("로그아웃 처리 중 오류가 발생했습니다.", 500));
    }
  }

  @Operation(summary = "회원 탈퇴", description = "사용자 계정을 탈퇴 처리합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "회원 탈퇴 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "401", description = "인증 실패 또는 비밀번호 불일치"),
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @PostMapping("/withdrawal")
  public ResponseEntity<GrobleResponse<Void>> withdrawUser(
      @Auth Accessor accessor,
      @Valid @RequestBody UserWithdrawalRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {

    // 1. 회원 탈퇴 처리
    UserWithdrawalDto userWithdrawalDto = authDtoMapper.toServiceUserWithdrawalDto(request);

    authService.withdrawUser(accessor.getUserId(), userWithdrawalDto);

    // 2. 쿠키 삭제
    CookieUtils.deleteCookie(servletRequest, servletResponse, ACCESS_TOKEN_COOKIE_NAME);
    CookieUtils.deleteCookie(servletRequest, servletResponse, REFRESH_TOKEN_COOKIE_NAME);

    // 3. 응답 반환
    return ResponseEntity.ok().body(GrobleResponse.success(null, "회원 탈퇴가 성공적으로 처리되었습니다.", 200));
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

    // 4. 도메인 설정 (app.cookie.domain 프로퍼티 사용)
    String domain = null;
    if (!isLocal && cookieDomain != null && !cookieDomain.isEmpty()) {
      // 점으로 시작하지 않는 도메인 사용 (RFC 6265 준수)
      domain = cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain;
      // 또는 더 간단하게: domain = cookieDomain;
    }
    // 로컬 환경에서는 domain 명시적으로 설정하지 않음 (기본값 사용)

    // 5. 토큰 쿠키 설정
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
