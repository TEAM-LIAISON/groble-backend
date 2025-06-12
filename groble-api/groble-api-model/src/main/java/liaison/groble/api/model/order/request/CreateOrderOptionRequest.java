package liaison.groble.api.model.order.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "주문 옵션 요청")
public class CreateOrderOptionRequest {
  @NotNull(message = "옵션 ID는 필수입니다.")
  @Schema(description = "옵션 ID", example = "101")
  private Long optionId;

  @NotNull(message = "옵션 타입은 필수입니다.")
  @Schema(
      description = "옵션 타입",
      example = "COACHING_OPTION",
      allowableValues = {"COACHING_OPTION", "DOCUMENT_OPTION"})
  private OptionType optionType;

  @NotNull(message = "수량은 필수입니다.")
  @Positive(message = "수량은 1개 이상이어야 합니다.")
  @Schema(description = "구매 수량", example = "2")
  private Integer quantity;

  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
