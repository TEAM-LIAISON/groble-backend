package liaison.groble.api.model.auth.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import liaison.groble.api.model.terms.enums.TermsTypeDto;

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

  @NotEmpty(message = "약관 유형은 필수입니다.")
  @Schema(
      description = "약관 동의 유형",
      example = "[\"MARKETING\", \"TERMS\"]",
      allowableValues = {"ACTIVE", "DRAFT", "PENDING", "VALIDATED", "REJECTED"})
  private List<TermsTypeDto> termsTypes;

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

  @NotBlank(message = "닉네임은 필수 입력값입니다.")
  @Pattern(
      regexp = "^[가-힣a-zA-Z0-9]{2,15}$",
      message = "닉네임은 한글, 영문, 숫자만 사용할 수 있으며 2~15자 이내여야 합니다.")
  @Schema(description = "닉네임", example = "nickname")
  private String nickname;
}
