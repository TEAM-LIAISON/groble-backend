package liaison.groble.mapping.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AuthMapper {

  // ====== 📥 Request → DTO 변환 ======
  /** [통합 로그인] SignInRequest → SignInDTO */
  SignInDTO toSignInDto(SignInRequest request);

  /** [통합 회원가입] SignUpRequest → SignUpDto */
  @Mapping(
      target = "termsTypeStrings",
      expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
  SignUpDto toSignUpDto(SignUpRequest request);

  // ====== 📤 DTO → Response 변환 ======

  /** (email + 인증 결과 DTO) → SignInResponse */
  @Mapping(target = "authenticated", constant = "true")
  SignInResponse toSignInResponse(String email, SignInAuthResultDTO dto);

  /** VerifyEmailCodeRequest → VerifyEmailCodeDTO */
  VerifyEmailCodeDTO toVerifyEmailCodeDto(VerifyEmailCodeRequest request);

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
