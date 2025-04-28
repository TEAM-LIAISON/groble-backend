package liaison.groble.api.model.order.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "주문 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
  private Long orderId;
}
