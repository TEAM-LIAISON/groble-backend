package liaison.groble.domain.coupon.enums;

import lombok.Getter;

@Getter
public enum CouponStatus {
  ISSUED("발급됨"),
  USED("사용됨"),
  EXPIRED("만료됨"),
  CANCELLED("취소됨");

  private final String description;

  CouponStatus(String description) {
    this.description = description;
  }
}
