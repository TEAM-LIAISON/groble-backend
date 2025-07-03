package liaison.groble.api.model.purchase.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaserContentReviewRequest {
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
