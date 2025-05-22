package liaison.groble.api.model.terms.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메이커 이용약관 동의 요청")
public class MakerTermsAgreementRequest {

  @NotNull(message = "메이커 이용약관 동의는 필수입니다.")
  @Schema(description = "메이커 이용약관 동의 여부", example = "true", required = true)
  private Boolean makerTermsAgreement;
}
