package liaison.groble.mapping.auth;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.PhoneNumberVerifyCodeRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.application.auth.dto.PhoneNumberVerifyCodeRequestDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T19:58:41+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AuthMapperImpl implements AuthMapper {

  @Override
  public SignInDTO toSignInDto(SignInRequest request) {
    if (request == null) {
      return null;
    }

    SignInDTO.SignInDTOBuilder signInDTO = SignInDTO.builder();

    if (request.getEmail() != null) {
      signInDTO.email(request.getEmail());
    }
    if (request.getPassword() != null) {
      signInDTO.password(request.getPassword());
    }

    return signInDTO.build();
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

    signUpDto.termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList());

    return signUpDto.build();
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
      signInResponse.hasVerifiedPhoneNumber(dto.isHasVerifiedPhoneNumber());
    }
    if (email != null) {
      signInResponse.email(email);
    }
    signInResponse.authenticated(true);

    return signInResponse.build();
  }

  @Override
  public VerifyEmailCodeDTO toVerifyEmailCodeDto(VerifyEmailCodeRequest request) {
    if (request == null) {
      return null;
    }

    VerifyEmailCodeDTO.VerifyEmailCodeDTOBuilder verifyEmailCodeDTO = VerifyEmailCodeDTO.builder();

    if (request.getEmail() != null) {
      verifyEmailCodeDTO.email(request.getEmail());
    }
    if (request.getVerificationCode() != null) {
      verifyEmailCodeDTO.verificationCode(request.getVerificationCode());
    }

    return verifyEmailCodeDTO.build();
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
