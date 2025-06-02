package liaison.groble.api.model.coupon.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 쿠폰 정보 응답")
public class UserCouponResponse {
  @Schema(description = "쿠폰 코드", example = "XYZ1Q5DS311SAZ")
  private String couponCode;

  @Schema(description = "쿠폰 이름", example = "회원가입 쿠폰")
  private String name;

  @Schema(
      description = "쿠폰 유형",
      example = "PERCENTAGE",
      allowableValues = {"PERCENTAGE", "FIXED_PRICE"})
  private String couponType;

  @Schema(description = "할인 금액 (couponType 따라 다름)", example = "n(%/원)")
  private BigDecimal discountValue;

  @Schema(description = "쿠폰 유효 기간", example = "2023-12-31T23:59:59")
  private LocalDateTime validUntil;

  @Schema(description = "최소 주문 금액", example = "10000")
  private BigDecimal minOrderPrice;
}
