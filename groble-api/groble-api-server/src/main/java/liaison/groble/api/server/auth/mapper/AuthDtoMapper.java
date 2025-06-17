package liaison.groble.api.server.auth.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;

@Component
public class AuthDtoMapper {

  public SignInDto toServiceSignInDto(SignInRequest request) {
    return SignInDto.builder().email(request.getEmail()).password(request.getPassword()).build();
  }

  public EmailVerificationDto toServiceEmailVerificationDto(EmailVerificationRequest request) {
    return EmailVerificationDto.builder().email(request.getEmail()).build();
  }

  // 추가된 매퍼 메서드
  public VerifyEmailCodeDto toServiceVerifyEmailCodeDto(VerifyEmailCodeRequest request) {
    return VerifyEmailCodeDto.builder()
        .email(request.getEmail())
        .verificationCode(request.getVerificationCode())
        .build();
  }

  public UserWithdrawalDto toServiceUserWithdrawalDto(UserWithdrawalRequest userWithdrawalRequest) {
    return UserWithdrawalDto.builder()
        .reason(userWithdrawalRequest.getReason().name())
        .additionalComment(userWithdrawalRequest.getAdditionalComment())
        .build();
  }
}
