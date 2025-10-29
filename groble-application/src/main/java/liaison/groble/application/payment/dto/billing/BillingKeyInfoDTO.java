package liaison.groble.application.payment.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BillingKeyInfoDTO {
  private final String billingKey;
  private final String cardName;
  private final String cardNumberMasked;
}
