package liaison.groble.api.model.order.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {

  @NotNull(message = "콘텐츠 ID는 필수입니다.")
  @Schema(description = "구매할 콘텐츠 ID", example = "1")
  private Long contentId;

  @NotEmpty(message = "최소 하나 이상의 옵션을 입력해야 합니다.")
  @Valid
  @Schema(description = "선택한 옵션 목록 [최소 1개 이상]")
  private List<CreateOrderOptionRequest> options;

  @Schema(description = "쿠폰 코드 목록 (선택사항)", example = "[\"WELCOME10\", \"SALE50\"]")
  private List<String> couponCodes;

  @AssertTrue(message = "주문 약관 동의는 필수입니다.")
  @Schema(description = "주문 약관 동의 여부", example = "true")
  private boolean orderTermsAgreed;
}
