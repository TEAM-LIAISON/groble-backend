package liaison.groble.mapping.auth;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.api.model.auth.response.SignInTestResponse;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.SignUpDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:48:50+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AuthMapperImpl implements AuthMapper {

  @Override
  public SignInDTO toSignInDTO(SignInRequest request) {
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
  public SignUpDTO toSignUpDTO(SignUpRequest request) {
    if (request == null) {
      return null;
    }

    SignUpDTO.SignUpDTOBuilder signUpDTO = SignUpDTO.builder();

    if (request.getUserType() != null) {
      signUpDTO.userType(request.getUserType());
    }
    if (request.getEmail() != null) {
      signUpDTO.email(request.getEmail());
    }
    if (request.getPassword() != null) {
      signUpDTO.password(request.getPassword());
    }

    signUpDTO.termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList());

    return signUpDTO.build();
  }

  @Override
  public UserWithdrawalDTO toUserWithdrawalDTO(UserWithdrawalRequest request) {
    if (request == null) {
      return null;
    }

    UserWithdrawalDTO.UserWithdrawalDTOBuilder userWithdrawalDTO = UserWithdrawalDTO.builder();

    if (request.getAdditionalComment() != null) {
      userWithdrawalDTO.additionalComment(request.getAdditionalComment());
    }

    userWithdrawalDTO.reason(request.getReason().name());

    return userWithdrawalDTO.build();
  }

  @Override
  public SignInResponse toSignInResponse(String email, SignInAuthResultDTO signInAuthResultDTO) {
    if (email == null && signInAuthResultDTO == null) {
      return null;
    }

    SignInResponse.SignInResponseBuilder signInResponse = SignInResponse.builder();

    if (signInAuthResultDTO != null) {
      signInResponse.hasAgreedToTerms(signInAuthResultDTO.isHasAgreedToTerms());
      signInResponse.hasNickname(signInAuthResultDTO.isHasNickname());
      signInResponse.hasVerifiedPhoneNumber(signInAuthResultDTO.isHasVerifiedPhoneNumber());
    }
    if (email != null) {
      signInResponse.email(email);
    }
    signInResponse.authenticated(true);

    return signInResponse.build();
  }

  @Override
  public SignInTestResponse toSignInTestResponse(
      String email, SignInAuthResultDTO signInAuthResultDTO) {
    if (email == null && signInAuthResultDTO == null) {
      return null;
    }

    SignInTestResponse.SignInTestResponseBuilder signInTestResponse = SignInTestResponse.builder();

    if (signInAuthResultDTO != null) {
      signInTestResponse.hasAgreedToTerms(signInAuthResultDTO.isHasAgreedToTerms());
      signInTestResponse.hasNickname(signInAuthResultDTO.isHasNickname());
      if (signInAuthResultDTO.getAccessToken() != null) {
        signInTestResponse.accessToken(signInAuthResultDTO.getAccessToken());
      }
      if (signInAuthResultDTO.getRefreshToken() != null) {
        signInTestResponse.refreshToken(signInAuthResultDTO.getRefreshToken());
      }
    }
    if (email != null) {
      signInTestResponse.email(email);
    }
    signInTestResponse.authenticated(true);

    return signInTestResponse.build();
  }
}
