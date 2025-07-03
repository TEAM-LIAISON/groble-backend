package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelDTO {
  private String cancelReason;
  private String detailReason;
}
