package liaison.groble.domain.coupon.enums;

import lombok.Getter;

// CouponType.java - 쿠폰 타입 enum
@Getter
public enum CouponType {
  PERCENTAGE("퍼센트 할인"),
  FIXED_AMOUNT("금액 할인");

  private final String description;

  CouponType(String description) {
    this.description = description;
  }
}
