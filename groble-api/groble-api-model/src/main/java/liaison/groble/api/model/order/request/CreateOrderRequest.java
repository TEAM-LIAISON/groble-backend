package liaison.groble.api.model.order.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
  private Long gigId;
  private BigDecimal price;
  private int quantity;
  private BigDecimal totalPrice;
}
