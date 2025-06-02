package liaison.groble.api.model.payment.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompleteResponse {
  private String orderId;
  private String status;
  private BigDecimal price;
  private String productName;
  private LocalDateTime paymentDate;
}
