package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInfoDto {
  private String orderId;
  private Long userId;
  private BigDecimal price;
  private String payMethod;
  private String status;
  private String productName;
  private LocalDateTime paymentDate;

  private String cardName;
  private String cardNumber;
  private String receiptUrl;
}
