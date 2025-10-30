package liaison.groble.application.payment.dto.billing;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPaymentResult {
  private final String merchantUid;
  private final boolean success;
  private final String status;
  private final String message;
  private final BigDecimal totalAmount;
  private final SubscriptionPaymentMetadata metadata;
}
