package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.enums.AccountTypeDto;
import liaison.groble.api.model.user.enums.ProviderTypeDto;
import liaison.groble.api.model.user.enums.UserTypeDto;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.UserMyPageSummaryResponse;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;

@Component
public class UserDtoMapper {

  public UserMyPageSummaryResponse toApiMyPageSummaryResponse(
      UserMyPageSummaryDto userMyPageSummaryDto) {

    UserTypeDto userTypeDto =
        userMyPageSummaryDto.getUserTypeDescription() != null
            ? UserTypeDto.valueOf(userMyPageSummaryDto.getUserTypeDescription())
            : null;

    return UserMyPageSummaryResponse.builder()
        .nickName(userMyPageSummaryDto.getNickName())
        .profileImageUrl(userMyPageSummaryDto.getProfileImageUrl())
        .userType(userTypeDto)
        .canSwitchToSeller(userMyPageSummaryDto.isCanSwitchToSeller())
        .build();
  }

  public UserMyPageDetailResponse toApiMyPageDetailResponse(
      UserMyPageDetailDto userMyPageDetailDto) {
    AccountTypeDto accountTypeDto =
        userMyPageDetailDto.getAccountTypeName() != null
            ? AccountTypeDto.valueOf(userMyPageDetailDto.getAccountTypeName())
            : null;

    ProviderTypeDto providerTypeDto =
        userMyPageDetailDto.getProviderTypeName() != null
            ? ProviderTypeDto.valueOf(userMyPageDetailDto.getProviderTypeName())
            : null;

    return UserMyPageDetailResponse.builder()
        .nickName(userMyPageDetailDto.getNickName())
        .accountType(accountTypeDto)
        .providerType(providerTypeDto)
        .email(userMyPageDetailDto.getEmail())
        .profileImageUrl(userMyPageDetailDto.getProfileImageUrl())
        .phoneNumber(userMyPageDetailDto.getPhoneNumber())
        .sellerAccountNotCreated(userMyPageDetailDto.isSellerAccountNotCreated())
        .build();
  }
}
