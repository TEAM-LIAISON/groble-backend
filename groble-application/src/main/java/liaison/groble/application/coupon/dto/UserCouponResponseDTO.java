package liaison.groble.application.coupon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCouponResponseDTO {
  private String couponCode;
  private String name;
  private String couponType;
  private BigDecimal discountValue;
  private LocalDateTime validUntil;
  private BigDecimal minOrderPrice;
}
