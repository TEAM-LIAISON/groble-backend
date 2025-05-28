package liaison.groble.api.model.payment.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleAuthResponse {
  private String clientKey;
  private String authKey;
  private String returnUrl;
}
