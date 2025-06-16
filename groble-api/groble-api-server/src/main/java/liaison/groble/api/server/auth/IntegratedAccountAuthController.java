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
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDto;
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
@RequestMapping("/api/v1/auth")
@Tag(name = "통합 로그인 기능 관련 API", description = "통합 로그인 기능 API")
public class IntegratedAccountAuthController {
  private final AuthMapper authMapper;
  private final IntegratedAccountAuthService integratedAccountAuthService;
  private final TokenCookieService tokenCookieService;

  @Operation(summary = "통합 계정으로 로그인", description = "이메일과 비밀번호로 로그인하고(통합 계정 로그인) 인증 토큰을 발급합니다.")
  @PostMapping("/integrated/sign-in")
  public ResponseEntity<GrobleResponse<SignInResponse>> integratedAccountSignIn(
      @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody SignInRequest request,
      HttpServletResponse response) {
    log.info("통합 로그인 요청: {}", request.getEmail());

    SignInDto signInDto = authMapper.toSignInDto(request);
    SignInAuthResultDTO signInAuthResultDTO =
        integratedAccountAuthService.integratedAccountSignIn(signInDto);

    tokenCookieService.addTokenCookies(
        response, signInAuthResultDTO.getAccessToken(), signInAuthResultDTO.getRefreshToken());

    SignInResponse signInResponse =
        authMapper.toSignInResponse(signInDto.getEmail(), signInAuthResultDTO);

    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(signInResponse, "로그인이 성공적으로 완료되었습니다."));
  }
}
