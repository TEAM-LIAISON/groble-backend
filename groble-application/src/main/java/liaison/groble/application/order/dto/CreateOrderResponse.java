package liaison.groble.application.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrderResponse {
  // 주문 생성에 고유한 식별자
  private final String merchantUid;
  private final String email;
  private final String phoneNumber;
  private final String contentTitle;
  private final BigDecimal totalPrice;

  @Builder.Default private final Boolean isPurchasedContent = false;
}
