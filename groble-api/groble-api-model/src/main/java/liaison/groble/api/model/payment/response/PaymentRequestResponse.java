package liaison.groble.api.model.payment.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequestResponse {
  private String orderId;
  private BigDecimal price;
  private String productName;
  private String status;
}
