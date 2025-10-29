package liaison.groble.application.payment.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterBillingKeyCommand {
  private String billingKey;
  private String cardName;
  private String cardNumberMasked;
}
