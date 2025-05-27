package liaison.groble.domain.coupon.repository;

import java.util.List;

import liaison.groble.domain.coupon.dto.FlatUserCouponCardDTO;

public interface UserCouponCustomRepository {
  List<FlatUserCouponCardDTO> findAllUsableCouponsByUserId(Long userId);
}
