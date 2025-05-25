package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingPaymentRequest {
  private Long userId;
  private String billingKey;
  private BigDecimal amount;
  private String productName;
  private String userName;
  private String userPhone;
  private String userEmail;
}
