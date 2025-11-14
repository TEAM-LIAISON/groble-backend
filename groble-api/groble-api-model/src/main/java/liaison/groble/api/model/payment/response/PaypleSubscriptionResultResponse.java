package liaison.groble.api.model.payment.response;

import java.math.BigDecimal;

import liaison.groble.api.model.content.response.pay.ContentPayPageResponse;
import liaison.groble.api.model.order.response.CreateOrderResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "페이플 정기결제 처리 결과")
public class PaypleSubscriptionResultResponse {

  @Schema(description = "주문 식별자", example = "ORD202410120001")
  private String merchantUid;

  @Schema(description = "처리 상태", example = "PAID")
  private String status;

  @Schema(description = "페이플 응답 메시지", example = "승인완료")
  private String message;

  @Schema(description = "결제 금액", example = "33000")
  private BigDecimal totalAmount;

  @Schema(description = "정기결제 메타 정보")
  private ContentPayPageResponse.SubscriptionMetaResponse subscriptionMeta;

  @Schema(description = "Payple SDK 구성에 필요한 옵션")
  private CreateOrderResponse.PaypleOptionsResponse paypleOptions;
}
