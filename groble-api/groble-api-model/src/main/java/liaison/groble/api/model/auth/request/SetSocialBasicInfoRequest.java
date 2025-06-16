package liaison.groble.api.model.auth.request;

import java.util.List;

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
public class SetSocialBasicInfoRequest {

  @NotNull(message = "사용자 유형은 필수 입력값입니다.")
  @Pattern(regexp = "^(SELLER|BUYER)$", message = "사용자 유형은 SELLER 또는 BUYER만 가능합니다.")
  @Schema(description = "사용자 유형", example = "SELLER")
  private String userType;

  @NotEmpty(message = "약관 동의 유형은 필수입니다.")
  @Schema(
      description = "약관 동의 유형",
      example =
          "[\"AGE_POLICY\", \"PRIVACY_POLICY\", \"SERVICE_TERMS_POLICY\", \"SELLER_TERMS_POLICY\", \"MARKETING_POLICY\"]",
      allowableValues = {
        "AGE_POLICY",
        "PRIVACY_POLICY",
        "SERVICE_TERMS_POLICY",
        "SELLER_TERMS_POLICY",
        "MARKETING_POLICY"
      })
  private List<TermsTypeDto> termsTypes;
}
