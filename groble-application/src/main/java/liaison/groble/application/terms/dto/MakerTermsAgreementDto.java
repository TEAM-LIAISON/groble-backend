package liaison.groble.application.terms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakerTermsAgreementDto {

  private Long userId;
  private Boolean makerTermsAgreement;
}
