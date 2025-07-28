package liaison.groble.mapping.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.UserWithdrawalRequest;
import liaison.groble.api.model.auth.response.SignInResponse;
import liaison.groble.api.model.auth.response.SignInTestResponse;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.SignUpDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AuthMapper {

  // ====== 📥 Request → DTO 변환 ======
  /** [통합 로그인] SignInRequest → SignInDTO */
  SignInDTO toSignInDTO(SignInRequest request);

  /** [통합 회원가입] SignUpRequest → SignUpDTO */
  @Mapping(
      target = "termsTypeStrings",
      expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
  SignUpDTO toSignUpDTO(SignUpRequest request);

  /** UserWithdrawalRequest → UserWithdrawalDTO */
  @Mapping(target = "reason", expression = "java(request.getReason().name())")
  UserWithdrawalDTO toUserWithdrawalDTO(UserWithdrawalRequest request);

  // ====== 📤 DTO → Response 변환 ======

  /** (email + 인증 결과 DTO) → SignInResponse */
  @Mapping(target = "authenticated", constant = "true")
  SignInResponse toSignInResponse(String email, SignInAuthResultDTO signInAuthResultDTO);

  @Mapping(target = "email", source = "email")
  @Mapping(target = "authenticated", constant = "true")
  SignInTestResponse toSignInTestResponse(String email, SignInAuthResultDTO signInAuthResultDTO);
}
