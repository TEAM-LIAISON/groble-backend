package liaison.groble.mapping.coupon;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.application.coupon.dto.UserCouponResponseDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface CouponMapper {
  // ====== 📤 DTO → Response 변환 ======
  List<UserCouponResponse> toUserCouponResponseList(
      List<UserCouponResponseDTO> userCouponResponseDTOs);

  UserCouponResponse toUserCouponResponse(ContentPayPageDTO.UserCouponDTO userCouponDTO);
}
