package liaison.groble.api.model.order.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "초기 주문 생성 요청")
public class CreateInitialOrderRequest {

  @NotNull(message = "콘텐츠 ID는 필수입니다")
  @Schema(description = "구매할 콘텐츠 ID", example = "1")
  private Long contentId;

  @NotEmpty(message = "최소 하나 이상의 옵션을 선택해야 합니다")
  @Valid
  @Schema(description = "선택한 옵션 목록")
  private List<OrderOptionRequest> options;

  @Schema(description = "주문 메모", example = "빠른 배송 부탁드립니다")
  private String orderNote;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "주문 옵션 요청")
  public static class OrderOptionRequest {

    @NotNull(message = "옵션 ID는 필수입니다")
    @Schema(description = "옵션 ID", example = "1")
    private Long optionId;

    @NotNull(message = "옵션 타입은 필수입니다")
    @Schema(
        description = "옵션 타입",
        example = "COACHING_OPTION",
        allowableValues = {"COACHING_OPTION", "DOCUMENT_OPTION"})
    private OptionType optionType;

    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "구매 수량", example = "2", minimum = "1")
    private Integer quantity;
  }

  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
