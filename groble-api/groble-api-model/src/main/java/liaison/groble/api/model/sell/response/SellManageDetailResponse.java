package liaison.groble.api.model.sell.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 내 스토어 - 상품 관리 - 판매 관리] 판매 관리 페이지 정보에서 판매 관리 부분 응답")
public class SellManageDetailResponse {
  @Schema(
      description = "해당 콘텐츠에 대한 총 결제 금액",
      example = "600000",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalPaymentPrice;

  @Schema(
      description = "해당 콘텐츠에 대한 총 구매자 수",
      example = "500",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalPurchaseCustomer;

  @Schema(
      description = "해당 콘텐츠에 대한 총 리뷰 수",
      example = "30",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalReviewCount;
}
