package liaison.groble.api.model.order.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {

  @NotNull
  @Schema(description = "콘텐츠 ID", example = "1", required = true)
  private Long contentId;

  @Schema(
      description = "옵션 타입",
      example = "COACHING_OPTION",
      allowableValues = {"COACHING_OPTION", "DOCUMENT_OPTION"})
  private String optionType;

  @Schema(description = "옵션 ID", example = "1")
  private Long optionId;

  @Schema(description = "쿠폰 ID (선택사항)", example = "100")
  private Long couponId;
}
