package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayplePaymentLinkRequestDto {
  private Long orderId;
  private Long contentId;
  private Long optionId;
  private BigDecimal price;
  private int quantity;
  private BigDecimal totalPrice;
}
