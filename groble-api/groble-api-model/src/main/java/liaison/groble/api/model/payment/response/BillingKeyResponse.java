package liaison.groble.api.model.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BillingKeyResponse", description = "정기결제용 빌링키 응답")
public class BillingKeyResponse {

  @Schema(description = "등록된 페이플 빌링키", example = "PAYER1234567890")
  private String billingKey;

  @Schema(description = "카드사명", example = "신한카드")
  private String cardName;

  @Schema(description = "마스킹된 카드번호", example = "1234-****-****-5678")
  private String cardNumberMasked;
}
