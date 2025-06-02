package liaison.groble.api.model.payment.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInfo {
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
