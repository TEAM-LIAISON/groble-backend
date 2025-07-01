package liaison.groble.api.model.auth.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import liaison.groble.api.model.terms.enums.TermsTypeDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
  @NotNull(message = "사용자 유형은 필수 입력값입니다.")
  @Pattern(regexp = "^(SELLER|BUYER)$", message = "사용자 유형은 SELLER 또는 BUYER만 가능합니다.")
  @Schema(description = "사용자 유형", example = "SELLER")
  private String userType;

  @Schema(
      description =
          """
    가입 약관 동의 유형 리스트입니다. 다음 항목 중 하나 이상을 포함해야 합니다.

    - AGE_POLICY: 만 14세 이상입니다.
    - PRIVACY_POLICY: 개인정보 수집 및 이용 동의
    - SERVICE_TERMS_POLICY: 서비스 이용약관 동의
    - SELLER_TERMS_POLICY: 메이커 이용약관 동의
    - MARKETING_POLICY: 마케팅 활용 및 수신 동의
    """,
      example = "[\"AGE_POLICY\", \"PRIVACY_POLICY\", \"SERVICE_TERMS_POLICY\"]",
      allowableValues = {
        "AGE_POLICY",
        "PRIVACY_POLICY",
        "SERVICE_TERMS_POLICY",
        "SELLER_TERMS_POLICY",
        "MARKETING_POLICY"
      })
  @NotEmpty(message = "가입 약관 유형 선택은 필수입니다.")
  private List<TermsTypeDTO> termsTypes;

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "유효한 이메일 형식이 아닙니다.")
  @Schema(description = "인증된 이메일", example = "example@example.com")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하로 입력해주세요.")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
      message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
  @Schema(description = "비밀번호", example = "Password123!")
  private String password;
}
