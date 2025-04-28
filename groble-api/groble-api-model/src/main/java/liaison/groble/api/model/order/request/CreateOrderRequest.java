package liaison.groble.api.model.order.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

  //    PID
  //    days
  //    packageType
  //    price
  //    quantity
  //    selected_options
  //    total_price
  private Long gigId;
}
