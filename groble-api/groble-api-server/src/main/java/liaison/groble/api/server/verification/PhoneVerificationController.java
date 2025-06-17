package liaison.groble.api.server.verification;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.auth.request.PhoneNumberVerifyCodeRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyRequest;
import liaison.groble.api.model.auth.response.PhoneNumberResponse;
import liaison.groble.application.auth.service.PhoneAuthService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verification")
@Tag(name = "전화번호를 활용한 인증 관련 API", description = "전화번호 인증 코드 발송, 전화번호 인증 코드 검증")
public class PhoneVerificationController {

  private final PhoneAuthService phoneAuthService;

  /** 전화번호 인증 요청 - Optional 인증 로그인 사용자: 기존 사용자의 전화번호 변경/추가 인증 비로그인 사용자: 회원가입 전 전화번호 인증 */
  @Operation(summary = "전화번호 인증 요청", description = "전화번호 인증 코드를 발송합니다.")
  @PostMapping("/phone-number/verify-request")
  public ResponseEntity<GrobleResponse<PhoneNumberResponse>> authPhoneNumber(
      @Auth(required = false) Accessor accessor, // Optional 인증
      @Parameter(description = "전화번호 인증 정보", required = true) @Valid @RequestBody
          PhoneNumberVerifyRequest request) {

    if (accessor.isAuthenticated()) {
      phoneAuthService.sendVerificationCodeForUser(accessor.getId(), request.getPhoneNumber());
    } else {
      phoneAuthService.sendVerificationCodeForSignup(request.getPhoneNumber());
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(null, "전화번호 인증 요청이 성공적으로 완료되었습니다.", 200));
  }

  /** 전화번호 인증 코드 검증 - Optional 인증 로그인 사용자: 사용자별 인증 코드 검증 비로그인 사용자: 비회원 인증 코드 검증 */
  @Operation(summary = "전화번호 인증 코드 검증", description = "전화번호로 발송된 인증 코드를 검증합니다.")
  @PostMapping("/phone-number/verify-code")
  public ResponseEntity<GrobleResponse<PhoneNumberResponse>> verifyPhoneNumber(
      @Auth(required = false) Accessor accessor, // Optional 인증 추가
      @Parameter(description = "전화번호 인증 정보", required = true) @Valid @RequestBody
          PhoneNumberVerifyCodeRequest request) {

    if (accessor.isAuthenticated()) {
      phoneAuthService.verifyCodeForUser(
          accessor.getId(), request.getPhoneNumber(), request.getVerificationCode());
    } else {
      phoneAuthService.verifyCodeForSignup(request.getPhoneNumber(), request.getVerificationCode());
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(null, "전화번호 인증이 성공적으로 완료되었습니다.", 200));
  }
}
