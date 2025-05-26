package liaison.groble.domain.coupon.repository;

import java.util.Optional;

import liaison.groble.domain.coupon.entity.UserCoupon;

public interface UserCouponRepository {
  UserCoupon save(UserCoupon userCoupon);

  Optional<UserCoupon> findByCouponCode(String couponCode);
}
