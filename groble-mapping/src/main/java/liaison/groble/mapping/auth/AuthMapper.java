package liaison.groble.mapping.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyCodeRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyRequest;
import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.SocialSignUpRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyCodeRequestDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.SocialSignUpDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AuthMapper {

  /** SignUpRequest → SignUpDto */
  @Mapping(
      target = "termsTypeStrings",
      expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
  SignUpDto toSignUpDto(SignUpRequest request);

  /** SocialSignUpRequest → SocialSignUpDto */
  @Mapping(
      target = "termsTypeStrings",
      expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
  SocialSignUpDto toSocialSignUpDto(SocialSignUpRequest request);

  /** SignInRequest → SignInDto */
  SignInDto toSignInDto(SignInRequest request);

  /** EmailVerificationRequest → EmailVerificationDto */
  EmailVerificationDto toEmailVerificationDto(EmailVerificationRequest request);

  /** VerifyEmailCodeRequest → VerifyEmailCodeDto */
  VerifyEmailCodeDto toVerifyEmailCodeDto(VerifyEmailCodeRequest request);

  /** UserWithdrawalRequest → UserWithdrawalDto */
  @Mapping(target = "reason", expression = "java(request.getReason().name())")
  UserWithdrawalDto toUserWithdrawalDto(UserWithdrawalRequest request);

  /** PhoneNumberVerifyRequest → PhoneNumberVerifyRequestDto */
  PhoneNumberVerifyRequestDto toPhoneNumberVerifyRequestDto(PhoneNumberVerifyRequest request);

  /** PhoneNumberVerifyCodeRequest → PhoneNumberVerifyCodeRequestDto */
  @Mapping(source = "verificationCode", target = "verifyCode")
  PhoneNumberVerifyCodeRequestDto toPhoneNumberVerifyCodeRequestDto(
      PhoneNumberVerifyCodeRequest request);
}
