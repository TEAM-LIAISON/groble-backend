package liaison.groble.api.model.terms.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import liaison.groble.api.model.terms.enums.TermsTypeDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementRequest {
  @NotEmpty(message = "약관 유형은 필수입니다.")
  private List<TermsTypeDto> termsTypes;
}
