package liaison.groble.api.model.order.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {
  @Schema(description = "구매자 이메일 (결제완료, 취소 메일이 발송됩니다.)", example = "kwondm7@naver.com")
  private String email;

  @Schema(description = "구매자 휴대폰번호", example = "010-1234-5678")
  private String phoneNumber;

  @NotNull(message = "콘텐츠 ID는 필수입니다")
  @Schema(description = "구매할 콘텐츠 ID", example = "1")
  private Long contentId;

  @NotEmpty(message = "최소 하나 이상의 옵션을 입력해야 합니다")
  @Valid
  @Schema(description = "선택한 옵션 목록 [최소 1개 이상]")
  private List<OrderOptionRequest> options;

  @Schema(description = "쿠폰 코드 목록 (선택사항)", example = "[\"XYZ1Q5DS311SAZ\", \"ABC2Z8DK22PPP\"]")
  private List<String> couponCodes;

  @AssertTrue(message = "주문 약관에 반드시 동의해야 합니다")
  @Schema(description = "주문 약관 동의", example = "true")
  private boolean orderTermsAgreed;

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
    private CreateOrderRequest.OptionType optionType;

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
