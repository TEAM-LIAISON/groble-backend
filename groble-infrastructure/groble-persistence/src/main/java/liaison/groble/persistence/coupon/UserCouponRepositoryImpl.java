package liaison.groble.persistence.coupon;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.coupon.repository.UserCouponRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {
  private final JpaUserCouponRepository jpaUserCouponRepository;

  @Override
  public UserCoupon save(UserCoupon userCoupon) {
    return jpaUserCouponRepository.save(userCoupon);
  }

  @Override
  public Optional<UserCoupon> findByCouponCode(String couponCode) {
    return jpaUserCouponRepository.findByCouponCode(couponCode);
  }
}
