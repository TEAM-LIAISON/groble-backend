package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompleteResponseDto {
  private String orderId;
  private String status;
  private BigDecimal price;
  private String productName;
  private LocalDateTime paymentDate;
}
