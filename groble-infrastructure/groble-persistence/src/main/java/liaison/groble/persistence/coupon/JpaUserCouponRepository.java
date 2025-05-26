package liaison.groble.persistence.coupon;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.coupon.entity.UserCoupon;

public interface JpaUserCouponRepository extends JpaRepository<UserCoupon, Long> {

  Optional<UserCoupon> findByCouponCode(String couponCode);
}
