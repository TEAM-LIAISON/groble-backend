package liaison.groble.mapping.coupon;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.application.coupon.dto.UserCouponResponseDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-29T18:05:45+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class CouponMapperImpl implements CouponMapper {

  @Override
  public List<UserCouponResponse> toUserCouponResponseList(
      List<UserCouponResponseDTO> userCouponResponseDTOs) {
    if (userCouponResponseDTOs == null) {
      return null;
    }

    List<UserCouponResponse> list =
        new ArrayList<UserCouponResponse>(userCouponResponseDTOs.size());
    for (UserCouponResponseDTO userCouponResponseDTO : userCouponResponseDTOs) {
      list.add(userCouponResponseDTOToUserCouponResponse(userCouponResponseDTO));
    }

    return list;
  }

  @Override
  public UserCouponResponse toUserCouponResponse(ContentPayPageDTO.UserCouponDTO userCouponDTO) {
    if (userCouponDTO == null) {
      return null;
    }

    UserCouponResponse.UserCouponResponseBuilder userCouponResponse = UserCouponResponse.builder();

    if (userCouponDTO.getCouponCode() != null) {
      userCouponResponse.couponCode(userCouponDTO.getCouponCode());
    }
    if (userCouponDTO.getName() != null) {
      userCouponResponse.name(userCouponDTO.getName());
    }
    if (userCouponDTO.getCouponType() != null) {
      userCouponResponse.couponType(userCouponDTO.getCouponType());
    }
    if (userCouponDTO.getDiscountValue() != null) {
      userCouponResponse.discountValue(userCouponDTO.getDiscountValue());
    }
    if (userCouponDTO.getValidUntil() != null) {
      userCouponResponse.validUntil(userCouponDTO.getValidUntil());
    }
    if (userCouponDTO.getMinOrderPrice() != null) {
      userCouponResponse.minOrderPrice(userCouponDTO.getMinOrderPrice());
    }

    return userCouponResponse.build();
  }

  protected UserCouponResponse userCouponResponseDTOToUserCouponResponse(
      UserCouponResponseDTO userCouponResponseDTO) {
    if (userCouponResponseDTO == null) {
      return null;
    }

    UserCouponResponse.UserCouponResponseBuilder userCouponResponse = UserCouponResponse.builder();

    if (userCouponResponseDTO.getCouponCode() != null) {
      userCouponResponse.couponCode(userCouponResponseDTO.getCouponCode());
    }
    if (userCouponResponseDTO.getName() != null) {
      userCouponResponse.name(userCouponResponseDTO.getName());
    }
    if (userCouponResponseDTO.getCouponType() != null) {
      userCouponResponse.couponType(userCouponResponseDTO.getCouponType());
    }
    if (userCouponResponseDTO.getDiscountValue() != null) {
      userCouponResponse.discountValue(userCouponResponseDTO.getDiscountValue());
    }
    if (userCouponResponseDTO.getValidUntil() != null) {
      userCouponResponse.validUntil(userCouponResponseDTO.getValidUntil());
    }
    if (userCouponResponseDTO.getMinOrderPrice() != null) {
      userCouponResponse.minOrderPrice(userCouponResponseDTO.getMinOrderPrice());
    }

    return userCouponResponse.build();
  }
}
