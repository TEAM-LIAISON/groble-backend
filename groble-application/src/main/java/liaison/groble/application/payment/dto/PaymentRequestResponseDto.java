package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequestResponseDto {
  private String orderId;
  private BigDecimal price;
  private String productName;
  private String status;
}
