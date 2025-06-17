package liaison.groble.api.server.verification;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.ResetPasswordRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.EmailVerificationDTO;
import liaison.groble.application.auth.dto.VerifyEmailCodeDTO;
import liaison.groble.application.verification.VerificationService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.mapping.verification.VerificationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verification")
@Tag(
    name = "[이메일 인증] 이메일을 활용한 코드 전송, 코드 검증, 비밀번호 재설정 등 API",
    description = "이메일을 활용하여 회원가입 과정에서 이메일을 검증, 이메일 변경, 비밀번호 재설정 등의 기능을 제공합니다.")
public class EmailVerificationController {

  // API 경로 상수화
  private static final String SEND_CODE_FOR_SIGNUP = "/email/code/sign-up";
  private static final String VERIFY_CODE_FOR_SIGNUP = "/email/code/verify/sign-up";
  private static final String SEND_CODE_FOR_CHANGE_EMAIL = "/email/code/change-email";
  private static final String VERIFY_CODE_FOR_CHANGE_EMAIL = "/email/code/verify/change-email";
  private static final String SEND_PASSWORD_RESET = "/email/code/password-reset";
  private static final String RESET_PASSWORD = "/password/reset";

  private final VerificationMapper verificationMapper;
  private final VerificationService verificationService;

  @Operation(summary = "회원가입 이메일 인증 코드 발송", description = "통합 회원가입 과정에서 이메일로 4자리 인증 코드를 발송합니다.")
  @PostMapping(SEND_CODE_FOR_SIGNUP)
  public ResponseEntity<GrobleResponse<Void>> sendCodeForSignUp(
      @Parameter(description = "인증 코드를 받을 이메일 주소", required = true) @Valid @RequestBody
          EmailVerificationRequest request) {

    EmailVerificationDTO dto = verificationMapper.toEmailVerificationDTO(request);
    verificationService.sendEmailVerificationForSignUp(dto);

    return createSuccessResponse();
  }

  @Operation(summary = "회원가입 이메일 인증 코드 검증", description = "회원가입 시 발송된 4자리 인증 코드의 유효성을 검증합니다.")
  @PostMapping(VERIFY_CODE_FOR_SIGNUP)
  public ResponseEntity<GrobleResponse<Void>> verifyCodeForSignUp(
      @Parameter(description = "이메일과 인증 코드 정보", required = true) @Valid @RequestBody
          VerifyEmailCodeRequest request) {

    VerifyEmailCodeDTO dto = verificationMapper.toVerifyEmailCodeDTO(request);
    verificationService.verifyEmailCode(dto);

    return createSuccessResponse();
  }

  @Operation(summary = "이메일 변경 인증 코드 발송", description = "이메일 변경을 위한 인증 코드를 새 이메일 주소로 발송합니다.")
  @PostMapping(SEND_CODE_FOR_CHANGE_EMAIL)
  public ResponseEntity<GrobleResponse<Void>> sendCodeForChangeEmail(
      @Auth Accessor accessor,
      @Parameter(description = "변경할 새 이메일 주소", required = true) @Valid @RequestBody
          EmailVerificationRequest request) {

    EmailVerificationDTO dto = verificationMapper.toEmailVerificationDTO(request);
    verificationService.sendEmailVerificationForChangeEmail(accessor.getUserId(), dto);

    return createSuccessResponse();
  }

  @Operation(summary = "이메일 변경 인증 코드 검증", description = "이메일 변경을 위한 인증 코드를 검증하고 이메일을 업데이트합니다.")
  @PostMapping(VERIFY_CODE_FOR_CHANGE_EMAIL)
  public ResponseEntity<GrobleResponse<Void>> verifyCodeForChangeEmail(
      @Auth Accessor accessor,
      @Parameter(description = "새 이메일과 인증 코드 정보", required = true) @Valid @RequestBody
          VerifyEmailCodeRequest request) {

    VerifyEmailCodeDTO dto = verificationMapper.toVerifyEmailCodeDTO(request);

    verificationService.verifyAndUpdateEmail(accessor.getUserId(), dto);

    return createSuccessResponse();
  }

  @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 링크가 포함된 이메일을 발송합니다.")
  @PostMapping(SEND_PASSWORD_RESET)
  public ResponseEntity<GrobleResponse<Void>> sendPasswordResetEmail(
      @Parameter(description = "비밀번호 재설정 링크를 받을 이메일 주소", required = true) @Valid @RequestBody
          EmailVerificationRequest request) {

    verificationService.sendPasswordResetEmail(request.getEmail());

    return createSuccessResponse();
  }

  @Operation(summary = "비밀번호 재설정 실행", description = "토큰을 사용하여 새로운 비밀번호로 재설정합니다.")
  @PostMapping(RESET_PASSWORD)
  public ResponseEntity<GrobleResponse<Void>> resetPassword(
      @Parameter(description = "재설정 토큰과 새 비밀번호", required = true) @Valid @RequestBody
          ResetPasswordRequest request) {

    verificationService.resetPassword(request.getToken(), request.getNewPassword());

    return createSuccessResponse();
  }

  /** 성공 응답 생성 유틸리티 메서드 */
  private ResponseEntity<GrobleResponse<Void>> createSuccessResponse() {
    return ResponseEntity.ok(GrobleResponse.success());
  }
}
