package liaison.groble.api.server.coupon;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.api.model.coupon.response.UserCouponsResponse;
import liaison.groble.application.coupon.dto.UserCouponResponseDTO;
import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.mapping.coupon.CouponMapper;

import io.swagger.v3.oas.annotations.Operation;
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

  @Operation(summary = "내가 소유한 쿠폰 목록 조회", description = "내가 소유한 쿠폰 목록을 조회합니다.")
  @GetMapping(MY_COUPONS_PATH)
  public ResponseEntity<GrobleResponse<UserCouponsResponse>> getUserCoupons(
      @Auth Accessor accessor) {
    List<UserCouponResponseDTO> dto = couponService.getMyPageUserCoupons(accessor.getUserId());
    List<UserCouponResponse> userCouponResponses = couponMapper.toUserCouponResponseList(dto);

    UserCouponsResponse userCouponResponse = new UserCouponsResponse(userCouponResponses);
    return ResponseEntity.ok(GrobleResponse.success(userCouponResponse));
  }
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
