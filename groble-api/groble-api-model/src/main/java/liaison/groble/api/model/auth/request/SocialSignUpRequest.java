package liaison.groble.api.model.auth.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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
public class SocialSignUpRequest {

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

  @NotBlank(message = "닉네임은 필수 입력값입니다.")
  @Pattern(
      regexp = "^[가-힣a-zA-Z0-9]{2,15}$",
      message = "닉네임은 한글, 영문, 숫자만 사용할 수 있으며 2~15자 이내여야 합니다.")
  @Schema(description = "닉네임", example = "nickname")
  private String nickname;

  @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식으로 입력해주세요.")
  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;
}
