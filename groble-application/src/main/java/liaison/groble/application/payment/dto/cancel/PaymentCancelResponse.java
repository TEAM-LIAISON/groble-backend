package liaison.groble.application.payment.dto.cancel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelResponse {
  private String merchantUid;
  private String status;
  private LocalDateTime canceledAt;
  private String cancelReason;
  private BigDecimal refundAmount;
}
