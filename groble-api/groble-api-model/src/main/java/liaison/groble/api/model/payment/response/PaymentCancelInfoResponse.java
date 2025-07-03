package liaison.groble.api.model.payment.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelInfoResponse {
  @Schema(
      description = "취소 요청된 상품 ID",
      example = "20250623111550134",
      type = "String",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(
      description = "환불 상품 금액",
      example = "30900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal originalPrice;

  @Schema(
      description = "할인 금액",
      example = "1000",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal discountPrice;

  @Schema(
      description = "총 환불 금액",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal finalPrice;

  @Schema(
      description = "결제 수단",
      example = "card",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payType;

  @Schema(
      description = "결제 카드 이름",
      example = "하나(외환)",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardName;

  @Schema(
      description = "결제 카드 번호 (마스킹 처리)",
      example = "53275011****9548",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardNum;
}
