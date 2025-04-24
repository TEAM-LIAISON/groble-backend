package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApproveDto {
  private String paymentKey;
  private String orderId;
  private BigDecimal amount;
}
