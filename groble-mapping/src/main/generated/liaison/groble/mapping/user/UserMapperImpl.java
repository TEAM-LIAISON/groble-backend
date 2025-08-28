package liaison.groble.mapping.user;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.request.SetSocialBasicInfoRequest;
import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-28T00:29:15+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class UserMapperImpl implements UserMapper {

  @Override
  public SocialBasicInfoDTO toSocialBasicInfoDTO(SetSocialBasicInfoRequest request) {
    if (request == null) {
      return null;
    }

    SocialBasicInfoDTO.SocialBasicInfoDTOBuilder socialBasicInfoDTO = SocialBasicInfoDTO.builder();

    if (request.getUserType() != null) {
      socialBasicInfoDTO.userType(request.getUserType());
    }

    socialBasicInfoDTO.termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList());

    return socialBasicInfoDTO.build();
  }

  @Override
  public UserHeaderResponse toUserHeaderResponse(UserHeaderDTO userHeaderDTO) {
    if (userHeaderDTO == null) {
      return null;
    }

    UserHeaderResponse.UserHeaderResponseBuilder userHeaderResponse = UserHeaderResponse.builder();

    if (userHeaderDTO.getIsLogin() != null) {
      userHeaderResponse.isLogin(userHeaderDTO.getIsLogin());
    }
    if (userHeaderDTO.getNickname() != null) {
      userHeaderResponse.nickname(userHeaderDTO.getNickname());
    }
    if (userHeaderDTO.getEmail() != null) {
      userHeaderResponse.email(userHeaderDTO.getEmail());
    }
    if (userHeaderDTO.getProfileImageUrl() != null) {
      userHeaderResponse.profileImageUrl(userHeaderDTO.getProfileImageUrl());
    }
    userHeaderResponse.canSwitchToSeller(userHeaderDTO.isCanSwitchToSeller());
    userHeaderResponse.unreadNotificationCount(userHeaderDTO.getUnreadNotificationCount());
    userHeaderResponse.alreadyRegisteredAsSeller(userHeaderDTO.isAlreadyRegisteredAsSeller());
    if (userHeaderDTO.getLastUserType() != null) {
      userHeaderResponse.lastUserType(userHeaderDTO.getLastUserType());
    }

    return userHeaderResponse.build();
  }

  @Override
  public BuyerMyPageSummaryResponse toBuyerSummaryResponse(
      UserMyPageSummaryDTO userMyPageSummaryDTO) {
    if (userMyPageSummaryDTO == null) {
      return null;
    }

    BuyerMyPageSummaryResponse.BuyerMyPageSummaryResponseBuilder buyerMyPageSummaryResponse =
        BuyerMyPageSummaryResponse.builder();

    if (userMyPageSummaryDTO.getUserTypeName() != null) {
      buyerMyPageSummaryResponse.userType(userMyPageSummaryDTO.getUserTypeName());
    }
    buyerMyPageSummaryResponse.canSwitchToSeller(userMyPageSummaryDTO.isCanSwitchToSeller());
    buyerMyPageSummaryResponse.alreadyRegisteredAsSeller(
        userMyPageSummaryDTO.isAlreadyRegisteredAsSeller());
    if (userMyPageSummaryDTO.getNickname() != null) {
      buyerMyPageSummaryResponse.nickname(userMyPageSummaryDTO.getNickname());
    }
    if (userMyPageSummaryDTO.getProfileImageUrl() != null) {
      buyerMyPageSummaryResponse.profileImageUrl(userMyPageSummaryDTO.getProfileImageUrl());
    }

    return buyerMyPageSummaryResponse.build();
  }

  @Override
  public SellerMyPageSummaryResponse toSellerSummaryResponse(
      UserMyPageSummaryDTO userMyPageSummaryDTO) {
    if (userMyPageSummaryDTO == null) {
      return null;
    }

    SellerMyPageSummaryResponse.SellerMyPageSummaryResponseBuilder sellerMyPageSummaryResponse =
        SellerMyPageSummaryResponse.builder();

    if (userMyPageSummaryDTO.getUserTypeName() != null) {
      sellerMyPageSummaryResponse.userType(userMyPageSummaryDTO.getUserTypeName());
    }
    if (userMyPageSummaryDTO.getVerificationStatusName() != null) {
      sellerMyPageSummaryResponse.verificationStatus(
          userMyPageSummaryDTO.getVerificationStatusName());
    }
    sellerMyPageSummaryResponse.alreadyRegisteredAsSeller(
        userMyPageSummaryDTO.isAlreadyRegisteredAsSeller());
    if (userMyPageSummaryDTO.getNickname() != null) {
      sellerMyPageSummaryResponse.nickname(userMyPageSummaryDTO.getNickname());
    }
    if (userMyPageSummaryDTO.getProfileImageUrl() != null) {
      sellerMyPageSummaryResponse.profileImageUrl(userMyPageSummaryDTO.getProfileImageUrl());
    }

    return sellerMyPageSummaryResponse.build();
  }

  @Override
  public UserMyPageDetailResponse toApiMyPageDetailResponse(
      UserMyPageDetailDTO userMyPageDetailDTO) {
    if (userMyPageDetailDTO == null) {
      return null;
    }

    UserMyPageDetailResponse.UserMyPageDetailResponseBuilder userMyPageDetailResponse =
        UserMyPageDetailResponse.builder();

    if (userMyPageDetailDTO.getUserTypeName() != null) {
      userMyPageDetailResponse.userType(userMyPageDetailDTO.getUserTypeName());
    }
    if (userMyPageDetailDTO.getAccountTypeName() != null) {
      userMyPageDetailResponse.accountType(userMyPageDetailDTO.getAccountTypeName());
    }
    if (userMyPageDetailDTO.getProviderTypeName() != null) {
      userMyPageDetailResponse.providerType(userMyPageDetailDTO.getProviderTypeName());
    }
    if (userMyPageDetailDTO.getNickname() != null) {
      userMyPageDetailResponse.nickname(userMyPageDetailDTO.getNickname());
    }
    if (userMyPageDetailDTO.getEmail() != null) {
      userMyPageDetailResponse.email(userMyPageDetailDTO.getEmail());
    }
    if (userMyPageDetailDTO.getProfileImageUrl() != null) {
      userMyPageDetailResponse.profileImageUrl(userMyPageDetailDTO.getProfileImageUrl());
    }
    if (userMyPageDetailDTO.getPhoneNumber() != null) {
      userMyPageDetailResponse.phoneNumber(userMyPageDetailDTO.getPhoneNumber());
    }
    userMyPageDetailResponse.canSwitchToSeller(userMyPageDetailDTO.isCanSwitchToSeller());
    userMyPageDetailResponse.sellerAccountNotCreated(
        userMyPageDetailDTO.isSellerAccountNotCreated());
    if (userMyPageDetailDTO.getVerificationStatus() != null) {
      userMyPageDetailResponse.verificationStatus(userMyPageDetailDTO.getVerificationStatus());
    }
    userMyPageDetailResponse.alreadyRegisteredAsSeller(
        userMyPageDetailDTO.isAlreadyRegisteredAsSeller());

    return userMyPageDetailResponse.build();
  }
}
