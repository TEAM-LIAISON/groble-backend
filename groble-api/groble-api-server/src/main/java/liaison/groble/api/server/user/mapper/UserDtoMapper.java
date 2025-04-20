package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.enums.AccountTypeDto;
import liaison.groble.api.model.user.enums.ProviderTypeDto;
import liaison.groble.api.model.user.enums.UserTypeDto;
import liaison.groble.api.model.user.response.AccountTypeResponse;
import liaison.groble.api.model.user.response.ProviderTypeResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.UserMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserTypeResponse;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;

@Component
public class UserDtoMapper {

  public UserMyPageSummaryResponse toApiMyPageSummaryResponse(
      UserMyPageSummaryDto userMyPageSummaryDto) {
    UserTypeResponse userType =
        userMyPageSummaryDto.getUserTypeName() != null
            ? UserTypeResponse.from(UserTypeDto.valueOf(userMyPageSummaryDto.getUserTypeName()))
            : null;

    return UserMyPageSummaryResponse.builder()
        .nickname(userMyPageSummaryDto.getNickname())
        .profileImageUrl(userMyPageSummaryDto.getProfileImageUrl())
        .userType(userType)
        .canSwitchToSeller(userMyPageSummaryDto.isCanSwitchToSeller())
        .build();
  }

  public UserMyPageDetailResponse toApiMyPageDetailResponse(
      UserMyPageDetailDto userMyPageDetailDto) {
    AccountTypeResponse accountType =
        userMyPageDetailDto.getAccountTypeName() != null
            ? AccountTypeResponse.from(
                AccountTypeDto.valueOf(userMyPageDetailDto.getAccountTypeName()))
            : null;

    ProviderTypeResponse providerType =
        userMyPageDetailDto.getProviderTypeName() != null
            ? ProviderTypeResponse.from(
                ProviderTypeDto.valueOf(userMyPageDetailDto.getProviderTypeName()))
            : null;

    return UserMyPageDetailResponse.builder()
        .nickname(userMyPageDetailDto.getNickname())
        .accountType(accountType)
        .providerType(providerType)
        .email(userMyPageDetailDto.getEmail())
        .profileImageUrl(userMyPageDetailDto.getProfileImageUrl())
        .phoneNumber(userMyPageDetailDto.getPhoneNumber())
        .sellerAccountNotCreated(userMyPageDetailDto.isSellerAccountNotCreated())
        .build();
  }
}
