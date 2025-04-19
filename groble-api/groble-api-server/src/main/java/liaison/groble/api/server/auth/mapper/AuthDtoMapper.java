package liaison.groble.api.server.auth.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.DeprecatedSignUpRequest;
import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.DeprecatedSignUpDto;
import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;

@Component
public class AuthDtoMapper {

  public DeprecatedSignUpDto toServiceDeprecatedSignUpDto(DeprecatedSignUpRequest request) {
    return DeprecatedSignUpDto.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .build();
  }

  public SignUpDto toServiceSignUpDto(SignUpRequest request) {
    return SignUpDto.builder().email(request.getEmail()).password(request.getPassword()).build();
  }

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
}
