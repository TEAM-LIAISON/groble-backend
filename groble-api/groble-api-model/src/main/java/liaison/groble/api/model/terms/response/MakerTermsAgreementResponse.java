package liaison.groble.api.model.terms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "메이커 이용약관 동의 응답")
public class MakerTermsAgreementResponse {

  @Schema(description = "사용자 ID", example = "123")
  private final Long userId;

  @Schema(description = "메이커 이용약관 동의 여부", example = "true")
  private final Boolean makerTermsAgreement;

  public static MakerTermsAgreementResponse of(Long userId, Boolean agreed) {
    return new MakerTermsAgreementResponse(userId, agreed);
  }
}
