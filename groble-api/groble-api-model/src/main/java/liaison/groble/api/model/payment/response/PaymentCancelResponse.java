package liaison.groble.api.model.payment.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelResponse {
  private String orderId;
  private String status;
  private LocalDateTime canceledAt;
  private String cancelReason;
}
