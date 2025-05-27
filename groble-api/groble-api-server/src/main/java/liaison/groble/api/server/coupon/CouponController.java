package liaison.groble.api.server.coupon;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.api.model.coupon.response.UserCouponsResponse;
import liaison.groble.api.server.coupon.mapper.CouponMapper;
import liaison.groble.application.coupon.dto.UserCouponResponseDto;
import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
@Tag(name = "쿠폰 관련 API", description = "쿠폰 관련 API")
public class CouponController {

  private final CouponService couponService;
  private final CouponMapper couponMapper;

  @Operation(summary = "쿠폰 목록 조회", description = "사용자의 쿠폰 목록을 조회합니다.")
  @GetMapping("/user")
  public ResponseEntity<GrobleResponse<UserCouponsResponse>> getUserCoupons(
      @Auth Accessor accessor) {
    List<UserCouponResponseDto> userCouponResponseDtos =
        couponService.getUserCoupons(accessor.getUserId());

    List<UserCouponResponse> userCouponResponses =
        userCouponResponseDtos.stream().map(couponMapper::toUserCouponsFromCouponDto).toList();

    UserCouponsResponse userCouponResponse = new UserCouponsResponse(userCouponResponses);

    return ResponseEntity.ok(GrobleResponse.success(userCouponResponse));
  }
}
