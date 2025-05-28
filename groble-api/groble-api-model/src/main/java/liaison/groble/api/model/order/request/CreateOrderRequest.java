package liaison.groble.api.model.order.request;

import jakarta.validation.constraints.AssertTrue;
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

  @NotNull(message = "콘텐츠 ID는 필수입니다")
  @Schema(description = "콘텐츠 ID", example = "1", required = true)
  private Long contentId;

  @NotNull(message = "옵션 ID는 필수입니다")
  @Schema(description = "옵션 ID", example = "1")
  private Long optionId;

  @NotNull(message = "옵션 타입은 필수입니다")
  @Schema(
      description = "옵션 타입",
      example = "COACHING_OPTION",
      allowableValues = {"COACHING_OPTION", "DOCUMENT_OPTION"})
  private String optionType;

  @Schema(description = "쿠폰 코드 (선택사항)", example = "XYZ1Q5DS311SAZ")
  private String couponCode;

  @AssertTrue(message = "주문 약관에 반드시 동의해야 합니다")
  @Schema(description = "주문 약관 동의", example = "true")
  private boolean orderTermsAgreed;
}
