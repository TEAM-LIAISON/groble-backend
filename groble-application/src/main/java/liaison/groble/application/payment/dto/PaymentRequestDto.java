package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequestDto {
  private BigDecimal amount;
  private String payMethod;
  private String productName;
  private String userName;
  private String userPhone;
  private String userEmail;
}
