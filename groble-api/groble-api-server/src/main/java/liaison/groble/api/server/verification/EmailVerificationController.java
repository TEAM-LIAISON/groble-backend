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
@RequestMapping("/api/v1/verification/email")
@Tag(name = "이메일을 활용한 인증 관련 API", description = "비밀번호 재설정 이메일 발송 기능, ")
public class EmailVerificationController {

  private final VerificationMapper verificationMapper;
  private final VerificationService verificationService;

  @Operation(
      summary = "이메일을 활용하여 통합 회원가입 과정에서 인증 코드를 발급합니다.",
      description = "사용자가 기입한 이메일에 인증 코드를 발급합니다.")
  @PostMapping("/code/sign-up")
  public ResponseEntity<GrobleResponse<Void>>
      sendIntegratedAccountEmailVerificationCodeForOnBoarding(
          @Parameter(description = "인증 코드를 발급받고자하는 이메일 정보", required = true) @Valid @RequestBody
              EmailVerificationRequest request) {
    EmailVerificationDTO emailVerificationDTO = verificationMapper.toEmailVerificationDTO(request);

    verificationService.sendEmailVerificationForSignUp(emailVerificationDTO);

    return ResponseEntity.ok().body(GrobleResponse.success());
  }

  @Operation(
      summary = "회원가입 과정에서 발급한 인증 코드를 검증합니다.",
      description = "이메일로 발송된 4자리 인증 코드의 유효성을 검증합니다.")
  @PostMapping("/code/verify/sign-up")
  public ResponseEntity<GrobleResponse<Void>> verifyEmailCode(
      @Parameter(description = "인증 코드(verificationCode)와 인증 코드를 수신한 이메일 정보", required = true)
          @Valid
          @RequestBody
          VerifyEmailCodeRequest request) {
    VerifyEmailCodeDTO verifyEmailCodeDTO = verificationMapper.toVerifyEmailCodeDTO(request);

    verificationService.verifyEmailCode(verifyEmailCodeDTO);

    return ResponseEntity.ok().body(GrobleResponse.success());
  }

  @Operation(summary = "이메일 변경 이메일 인증 요청", description = "사용자가 기입한 이메일에 인증 코드를 발급합니다.")
  @PostMapping("/code/change-email")
  public ResponseEntity<GrobleResponse<Void>> sendEmailVerificationForChangeEmail(
      @Auth Accessor accessor,
      @Parameter(description = "변경하고자 하는 이메일 정보(인증 코드를 수신할 이메일)", required = true)
          @Valid
          @RequestBody
          EmailVerificationRequest request) {
    EmailVerificationDTO emailVerificationDto = verificationMapper.toEmailVerificationDTO(request);

    verificationService.sendEmailVerificationForChangeEmail(
        accessor.getUserId(), emailVerificationDto);

    return ResponseEntity.ok().body(GrobleResponse.success());
  }

  @Operation(
      summary = "이메일 변경 시 이메일 인증 코드 확인",
      description = "이메일 변경 시 인증 코드의 유효성을 검증하고 이메일을 변경합니다.")
  @PostMapping("/code/verify/change-email")
  public ResponseEntity<GrobleResponse<Void>> verifyEmailCodeForChangeEmail(
      @Auth Accessor accessor, @Valid @RequestBody VerifyEmailCodeRequest request) {
    VerifyEmailCodeDTO verifyEmailCodeDto = verificationMapper.toVerifyEmailCodeDTO(request);

    verificationService.verifyEmailCodeForChangeEmail(accessor.getUserId(), verifyEmailCodeDto);

    return ResponseEntity.ok().body(GrobleResponse.success());
  }

  @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 링크가 포함된 이메일을 발송합니다.")
  @PostMapping("/code/password-reset")
  public ResponseEntity<GrobleResponse<Void>> requestPasswordReset(
      @Valid @RequestBody EmailVerificationRequest request) {

    verificationService.sendPasswordResetEmail(request.getEmail());

    return ResponseEntity.ok().body(GrobleResponse.success());
  }

  @Operation(summary = "비밀번호 재설정", description = "새로운 비밀번호로 재설정합니다.")
  @PostMapping("/password/reset")
  public ResponseEntity<GrobleResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {

    verificationService.resetPassword(request.getToken(), request.getNewPassword());

    return ResponseEntity.ok().body(GrobleResponse.success());
  }
}
