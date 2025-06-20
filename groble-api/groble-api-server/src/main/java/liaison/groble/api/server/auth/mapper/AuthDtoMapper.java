package liaison.groble.api.server.auth.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.EmailVerificationDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;
import liaison.groble.application.auth.dto.VerifyEmailCodeDTO;

@Component
public class AuthDtoMapper {

  public SignInDTO toServiceSignInDto(SignInRequest request) {
    return SignInDTO.builder().email(request.getEmail()).password(request.getPassword()).build();
  }

  public EmailVerificationDTO toServiceEmailVerificationDto(EmailVerificationRequest request) {
    return EmailVerificationDTO.builder().email(request.getEmail()).build();
  }

  // 추가된 매퍼 메서드
  public VerifyEmailCodeDTO toServiceVerifyEmailCodeDto(VerifyEmailCodeRequest request) {
    return VerifyEmailCodeDTO.builder()
        .email(request.getEmail())
        .verificationCode(request.getVerificationCode())
        .build();
  }

  public UserWithdrawalDTO toServiceUserWithdrawalDto(UserWithdrawalRequest userWithdrawalRequest) {
    return UserWithdrawalDTO.builder()
        .reason(userWithdrawalRequest.getReason().name())
        .additionalComment(userWithdrawalRequest.getAdditionalComment())
        .build();
  }
}
