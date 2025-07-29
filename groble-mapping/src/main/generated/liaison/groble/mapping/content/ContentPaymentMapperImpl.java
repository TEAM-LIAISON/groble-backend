package liaison.groble.mapping.content;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.pay.ContentPayPageResponse;
import liaison.groble.api.model.coupon.response.UserCouponResponse;
import liaison.groble.application.content.dto.ContentPayPageDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-29T18:05:45+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ContentPaymentMapperImpl implements ContentPaymentMapper {

  @Override
  public ContentPayPageResponse toContentPayPageResponse(ContentPayPageDTO contentPayPageDTO) {
    if (contentPayPageDTO == null) {
      return null;
    }

    ContentPayPageResponse.ContentPayPageResponseBuilder contentPayPageResponse =
        ContentPayPageResponse.builder();

    if (contentPayPageDTO.getIsLoggedIn() != null) {
      contentPayPageResponse.isLoggedIn(contentPayPageDTO.getIsLoggedIn());
    }
    if (contentPayPageDTO.getThumbnailUrl() != null) {
      contentPayPageResponse.thumbnailUrl(contentPayPageDTO.getThumbnailUrl());
    }
    if (contentPayPageDTO.getSellerName() != null) {
      contentPayPageResponse.sellerName(contentPayPageDTO.getSellerName());
    }
    if (contentPayPageDTO.getTitle() != null) {
      contentPayPageResponse.title(contentPayPageDTO.getTitle());
    }
    if (contentPayPageDTO.getContentType() != null) {
      contentPayPageResponse.contentType(contentPayPageDTO.getContentType());
    }
    if (contentPayPageDTO.getOptionName() != null) {
      contentPayPageResponse.optionName(contentPayPageDTO.getOptionName());
    }
    if (contentPayPageDTO.getPrice() != null) {
      contentPayPageResponse.price(contentPayPageDTO.getPrice());
    }
    List<UserCouponResponse> list =
        userCouponDTOListToUserCouponResponseList(contentPayPageDTO.getUserCoupons());
    if (list != null) {
      contentPayPageResponse.userCoupons(list);
    }

    return contentPayPageResponse.build();
  }

  protected UserCouponResponse userCouponDTOToUserCouponResponse(
      ContentPayPageDTO.UserCouponDTO userCouponDTO) {
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

  protected List<UserCouponResponse> userCouponDTOListToUserCouponResponseList(
      List<ContentPayPageDTO.UserCouponDTO> list) {
    if (list == null) {
      return null;
    }

    List<UserCouponResponse> list1 = new ArrayList<UserCouponResponse>(list.size());
    for (ContentPayPageDTO.UserCouponDTO userCouponDTO : list) {
      list1.add(userCouponDTOToUserCouponResponse(userCouponDTO));
    }

    return list1;
  }
}
