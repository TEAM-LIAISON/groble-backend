package liaison.groble.api.model.payment.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayplePaymentLinkRequest {
  @Schema(description = "주문 ID (orderId)", example = "12345678")
  private Long orderId;

  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @Schema(description = "선택된 옵션 ID", example = "1")
  private Long optionId;

  @Schema(description = "주문 가격", example = "5000")
  private BigDecimal price;

  @Schema(description = "주문 수량", example = "2")
  private int quantity;

  @Schema(description = "총 가격", example = "10000")
  private BigDecimal totalPrice;
}
