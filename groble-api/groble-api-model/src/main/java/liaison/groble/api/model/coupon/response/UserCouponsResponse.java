package liaison.groble.api.model.coupon.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 쿠폰 정보들 응답 리스트")
public class UserCouponsResponse {
  private List<UserCouponResponse> userCouponResponses;
}
