package liaison.groble.api.model.payment.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(name = "PaypleBillingChargeRequest", description = "빌링키 재과금 요청")
public class PaypleBillingChargeRequest {

  @NotBlank
  @Schema(description = "주문 식별자(merchantUid)", example = "ORD202410120001")
  private String merchantUid;
}
