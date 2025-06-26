package liaison.groble.api.server.coupon.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.application.coupon.dto.UserCouponResponseDTO;

@Component
public class CouponMapper {
  public UserCouponResponse toUserCouponResponse(UserCouponResponseDTO userCouponResponseDto) {
    return UserCouponResponse.builder()
        .couponCode(userCouponResponseDto.getCouponCode())
        .name(userCouponResponseDto.getName())
        .couponType(userCouponResponseDto.getCouponType())
        .discountValue(userCouponResponseDto.getDiscountValue())
        .validUntil(userCouponResponseDto.getValidUntil())
        .minOrderPrice(userCouponResponseDto.getMinOrderPrice())
        .build();
  }

  public UserCouponResponse toUserCouponsFromCouponDto(
      UserCouponResponseDTO userCouponResponseDto) {
    return UserCouponResponse.builder()
        .couponCode(userCouponResponseDto.getCouponCode())
        .name(userCouponResponseDto.getName())
        .couponType(userCouponResponseDto.getCouponType())
        .discountValue(userCouponResponseDto.getDiscountValue())
        .validUntil(userCouponResponseDto.getValidUntil())
        .minOrderPrice(userCouponResponseDto.getMinOrderPrice())
        .build();
  }
}
