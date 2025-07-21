package liaison.groble.domain.coupon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatUserCouponCardDTO {
  private String couponCode;
  private String title;
  private String couponType;
  private BigDecimal discountValue;
  private LocalDateTime validUntil;
  private BigDecimal minOrderPrice;
}
