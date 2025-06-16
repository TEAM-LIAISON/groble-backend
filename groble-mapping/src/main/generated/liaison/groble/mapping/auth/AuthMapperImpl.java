package liaison.groble.mapping.auth;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyCodeRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.SocialSignUpRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyCodeRequestDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.SocialSignUpDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T15:21:38+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AuthMapperImpl implements AuthMapper {

  @Override
  public SignInDto toSignInDto(SignInRequest request) {
    if (request == null) {
      return null;
    }

    SignInDto.SignInDtoBuilder signInDto = SignInDto.builder();

    if (request.getEmail() != null) {
      signInDto.email(request.getEmail());
    }
    if (request.getPassword() != null) {
      signInDto.password(request.getPassword());
    }

    return signInDto.build();
  }

  @Override
  public SignInResponse toSignInResponse(String email, SignInAuthResultDTO dto) {
    if (email == null && dto == null) {
      return null;
    }

    SignInResponse.SignInResponseBuilder signInResponse = SignInResponse.builder();

    if (dto != null) {
      signInResponse.hasAgreedToTerms(dto.isHasAgreedToTerms());
      signInResponse.hasNickname(dto.isHasNickname());
    }
    if (email != null) {
      signInResponse.email(email);
    }
    signInResponse.authenticated(true);

    return signInResponse.build();
  }

  @Override
  public SignUpDto toSignUpDto(SignUpRequest request) {
    if (request == null) {
      return null;
    }

    SignUpDto.SignUpDtoBuilder signUpDto = SignUpDto.builder();

    if (request.getUserType() != null) {
      signUpDto.userType(request.getUserType());
    }
    if (request.getEmail() != null) {
      signUpDto.email(request.getEmail());
    }
    if (request.getPassword() != null) {
      signUpDto.password(request.getPassword());
    }
    if (request.getNickname() != null) {
      signUpDto.nickname(request.getNickname());
    }
    if (request.getPhoneNumber() != null) {
      signUpDto.phoneNumber(request.getPhoneNumber());
    }

    signUpDto.termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList());

    return signUpDto.build();
  }

  @Override
  public SocialSignUpDto toSocialSignUpDto(SocialSignUpRequest request) {
    if (request == null) {
      return null;
    }

    SocialSignUpDto.SocialSignUpDtoBuilder socialSignUpDto = SocialSignUpDto.builder();

    if (request.getUserType() != null) {
      socialSignUpDto.userType(request.getUserType());
    }
    if (request.getNickname() != null) {
      socialSignUpDto.nickname(request.getNickname());
    }
    if (request.getPhoneNumber() != null) {
      socialSignUpDto.phoneNumber(request.getPhoneNumber());
    }

    socialSignUpDto.termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList());

    return socialSignUpDto.build();
  }

  @Override
  public EmailVerificationDto toEmailVerificationDto(EmailVerificationRequest request) {
    if (request == null) {
      return null;
    }

    EmailVerificationDto.EmailVerificationDtoBuilder emailVerificationDto =
        EmailVerificationDto.builder();

    if (request.getEmail() != null) {
      emailVerificationDto.email(request.getEmail());
    }

    return emailVerificationDto.build();
  }

  @Override
  public VerifyEmailCodeDto toVerifyEmailCodeDto(VerifyEmailCodeRequest request) {
    if (request == null) {
      return null;
    }

    VerifyEmailCodeDto.VerifyEmailCodeDtoBuilder verifyEmailCodeDto = VerifyEmailCodeDto.builder();

    if (request.getEmail() != null) {
      verifyEmailCodeDto.email(request.getEmail());
    }
    if (request.getVerificationCode() != null) {
      verifyEmailCodeDto.verificationCode(request.getVerificationCode());
    }

    return verifyEmailCodeDto.build();
  }

  @Override
  public UserWithdrawalDto toUserWithdrawalDto(UserWithdrawalRequest request) {
    if (request == null) {
      return null;
    }

    UserWithdrawalDto.UserWithdrawalDtoBuilder userWithdrawalDto = UserWithdrawalDto.builder();

    if (request.getAdditionalComment() != null) {
      userWithdrawalDto.additionalComment(request.getAdditionalComment());
    }

    userWithdrawalDto.reason(request.getReason().name());

    return userWithdrawalDto.build();
  }

  @Override
  public PhoneNumberVerifyRequestDto toPhoneNumberVerifyRequestDto(
      PhoneNumberVerifyRequest request) {
    if (request == null) {
      return null;
    }

    PhoneNumberVerifyRequestDto.PhoneNumberVerifyRequestDtoBuilder phoneNumberVerifyRequestDto =
        PhoneNumberVerifyRequestDto.builder();

    if (request.getPhoneNumber() != null) {
      phoneNumberVerifyRequestDto.phoneNumber(request.getPhoneNumber());
    }

    return phoneNumberVerifyRequestDto.build();
  }

  @Override
  public PhoneNumberVerifyCodeRequestDto toPhoneNumberVerifyCodeRequestDto(
      PhoneNumberVerifyCodeRequest request) {
    if (request == null) {
      return null;
    }

    PhoneNumberVerifyCodeRequestDto.PhoneNumberVerifyCodeRequestDtoBuilder
        phoneNumberVerifyCodeRequestDto = PhoneNumberVerifyCodeRequestDto.builder();

    if (request.getVerificationCode() != null) {
      phoneNumberVerifyCodeRequestDto.verifyCode(request.getVerificationCode());
    }
    if (request.getPhoneNumber() != null) {
      phoneNumberVerifyCodeRequestDto.phoneNumber(request.getPhoneNumber());
    }

    return phoneNumberVerifyCodeRequestDto.build();
  }
}
