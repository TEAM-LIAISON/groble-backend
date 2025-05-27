package liaison.groble.api.server.coupon.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.application.coupon.dto.UserCouponResponseDto;

@Component
public class CouponMapper {
  public UserCouponResponse toUserCouponResponse(UserCouponResponseDto userCouponResponseDto) {
    return UserCouponResponse.builder()
        .couponCode(userCouponResponseDto.getCouponCode())
        .name(userCouponResponseDto.getName())
        .couponType(userCouponResponseDto.getCouponType())
        .discountValue(userCouponResponseDto.getDiscountValue())
        .validUntil(userCouponResponseDto.getValidUntil())
        .minOrderAmount(userCouponResponseDto.getMinOrderAmount())
        .build();
  }

  public UserCouponResponse toUserCouponsFromCouponDto(
      UserCouponResponseDto userCouponResponseDto) {
    return UserCouponResponse.builder()
        .couponCode(userCouponResponseDto.getCouponCode())
        .name(userCouponResponseDto.getName())
        .couponType(userCouponResponseDto.getCouponType())
        .discountValue(userCouponResponseDto.getDiscountValue())
        .validUntil(userCouponResponseDto.getValidUntil())
        .minOrderAmount(userCouponResponseDto.getMinOrderAmount())
        .build();
  }
}
