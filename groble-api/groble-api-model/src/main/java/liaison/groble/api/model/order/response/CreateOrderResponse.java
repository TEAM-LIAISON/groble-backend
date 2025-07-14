package liaison.groble.api.model.order.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrderResponse {
  @Schema(
      description = "주문 식별 ID",
      example = "20251020349820",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(
      description = "결제를 진행하는 사용자 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "사용자 전화번호",
      example = "010-1234-5678",
      type = "string",
      pattern = "^\\d{3}-\\d{4}-\\d{4}$",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "콘텐츠 제목",
      example = "콘텐츠 제목입니다.",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  @Schema(
      description = "최종 가격",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalPrice;

  @Schema(
      description = "구매한 콘텐츠 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean isPurchasedContent;
}
