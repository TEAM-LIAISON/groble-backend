package liaison.groble.api.server.coupon;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.mapping.coupon.CouponMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupon")
@Tag(name = "[👨‍💻 마이페이지] 내가 소유한 쿠폰 목록 조회", description = "마이페이지 쿠폰 탭에서 내가 소유한 쿠폰 목록을 조회합니다.")
public class CouponController {

  private static final String MY_COUPONS_PATH = "/my-coupons";

  private final CouponService couponService;
  private final CouponMapper couponMapper;

  //  @Operation(summary = "내가 소유한 쿠폰 목록 조회", description = "내가 소유한 쿠폰 목록을 조회합니다.")
  //    @GetMapping(MY_COUPONS_PATH)
  //    public ResponseEntity<GrobleResponse<UserCouponsResponse>> getUserCoupons(
  //            @Auth Accessor accessor
  //    ) {
  //          List<UserCouponResponseDto> userCouponResponseDtos =
  //              couponService.getUserCoupons(accessor.getUserId());
  //
  //          List<UserCouponResponse> userCouponResponses =
  //
  // userCouponResponseDtos.stream().map(couponMapper::toUserCouponsFromCouponDto).toList();
  //    }
  //    List<UserCouponResponse> userCouponResponses =
  //        userCouponResponseDtos.stream().map(couponMapper::toUserCouponsFromCouponDto).toList();
  //
  //    UserCouponsResponse userCouponResponse = new UserCouponsResponse(userCouponResponses);
  //
  //    return ResponseEntity.ok(GrobleResponse.success(userCouponResponse));
  //  }
}
