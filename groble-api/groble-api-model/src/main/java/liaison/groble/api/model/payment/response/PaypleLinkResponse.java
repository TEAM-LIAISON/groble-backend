package liaison.groble.api.model.payment.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleLinkResponse {
  private String paymentResult;
  private String paymentLinkUrl;
}
