package liaison.groble.application.payment.dto.cancel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelDTO {
  private String cancelReason;
  private String detailReason;
}
