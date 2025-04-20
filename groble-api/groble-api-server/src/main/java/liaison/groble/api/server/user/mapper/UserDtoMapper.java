package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.enums.AccountTypeDto;
import liaison.groble.api.model.user.enums.ProviderTypeDto;
import liaison.groble.api.model.user.enums.UserTypeDto;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.UserMyPageSummaryResponse;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.common.response.EnumResponse;

@Component
public class UserDtoMapper {

  public UserMyPageSummaryResponse toApiMyPageSummaryResponse(
      UserMyPageSummaryDto userMyPageSummaryDto) {
    if (userMyPageSummaryDto.getUserTypeName() == null) {
      throw new IllegalArgumentException("userTypeName is NULL");
    }

    UserTypeDto userTypeDto = UserTypeDto.valueOf(userMyPageSummaryDto.getUserTypeName());

    return UserMyPageSummaryResponse.builder()
        .nickName(userMyPageSummaryDto.getNickName())
        .profileImageUrl(userMyPageSummaryDto.getProfileImageUrl())
        .userType(userTypeDto)
        .canSwitchToSeller(userMyPageSummaryDto.isCanSwitchToSeller())
        .build();
  }

  public UserMyPageDetailResponse toApiMyPageDetailResponse(
      UserMyPageDetailDto userMyPageDetailDto) {
    EnumResponse accountType =
        userMyPageDetailDto.getAccountTypeName() != null
            ? EnumResponse.from(AccountTypeDto.valueOf(userMyPageDetailDto.getAccountTypeName()))
            : null;

    EnumResponse providerType =
        userMyPageDetailDto.getProviderTypeName() != null
            ? EnumResponse.from(ProviderTypeDto.valueOf(userMyPageDetailDto.getProviderTypeName()))
            : null;

    return UserMyPageDetailResponse.builder()
        .nickName(userMyPageDetailDto.getNickName())
        .accountType(accountType)
        .providerType(providerType)
        .email(userMyPageDetailDto.getEmail())
        .profileImageUrl(userMyPageDetailDto.getProfileImageUrl())
        .phoneNumber(userMyPageDetailDto.getPhoneNumber())
        .sellerAccountNotCreated(userMyPageDetailDto.isSellerAccountNotCreated())
        .build();
  }
}
