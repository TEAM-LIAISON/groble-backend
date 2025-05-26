package liaison.groble.application.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateDto {
  private Long orderId;
  private Long contentId;
  private Long optionId;
  private BigDecimal price;
  private int quantity;
  private BigDecimal totalPrice;
}
