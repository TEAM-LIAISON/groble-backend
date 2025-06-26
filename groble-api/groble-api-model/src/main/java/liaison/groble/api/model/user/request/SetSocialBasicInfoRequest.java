package liaison.groble.api.model.user.request;

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
      description =
          """
        가입 약관 동의 유형 리스트입니다. 다음 항목 중 필수 값을 포함해야 합니다.

        - AGE_POLICY: 만 14세 이상입니다. (필수)
        - PRIVACY_POLICY: 개인정보 수집 및 이용 동의 (필수)
        - SERVICE_TERMS_POLICY: 서비스 이용약관 동의 (필수)
        - SELLER_TERMS_POLICY: 메이커 이용약관 동의 (판매자인 경우 필수)
        - MARKETING_POLICY: 마케팅 활용 및 수신 동의 (선택)
        """,
      example = "[\"AGE_POLICY\", \"PRIVACY_POLICY\", \"SERVICE_TERMS_POLICY\"]")
  private List<TermsTypeDto> termsTypes;
}
