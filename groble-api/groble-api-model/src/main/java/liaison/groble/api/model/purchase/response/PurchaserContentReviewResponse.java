package liaison.groble.api.model.purchase.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구매자의 콘텐츠 리뷰 응답 DTO")
public class PurchaserContentReviewResponse {
  @Schema(
      description = "리뷰 별점",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal rating;

  @Schema(
      description = "리뷰 내용",
      example = "좋아요",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String reviewContent;
}
