package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayplePaymentRequest {
  private Long userId;
  private BigDecimal amount;
  private String payMethod; // transfer, card
  private String productName;
  private String userName;
  private String userPhone;
  private String userEmail;
}
