package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.enums.AccountTypeDto;
import liaison.groble.api.model.user.enums.ProviderTypeDto;
import liaison.groble.api.model.user.enums.UserTypeDto;
import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.common.response.EnumResponse;

@Component
public class UserDtoMapper {

  // 중첩 구조 문제를 해결하기 위해 매퍼 메서드 수정
  public MyPageSummaryResponseBase toApiMyPageSummaryResponse(
      UserMyPageSummaryDto userMyPageSummaryDto) {

    String userTypeName = userMyPageSummaryDto.getUserTypeName();
    EnumResponse userType =
        userTypeName != null ? EnumResponse.from(UserTypeDto.valueOf(userTypeName)) : null;

    // 사용자 유형에 따라 다른 응답 객체 직접 반환 (래퍼 사용하지 않음)
    if (userTypeName != null && userTypeName.equals("SELLER")) {
      return toSellerSummaryResponse(userMyPageSummaryDto, userType);
    } else {
      return toBuyerSummaryResponse(userMyPageSummaryDto, userType);
    }
  }

  // 구매자 전용 응답 생성 메서드
  private BuyerMyPageSummaryResponse toBuyerSummaryResponse(
      UserMyPageSummaryDto dto, EnumResponse userType) {

    return BuyerMyPageSummaryResponse.builder()
        .nickname(dto.getNickname())
        .profileImageUrl(dto.getProfileImageUrl())
        .userType(userType)
        .canSwitchToSeller(dto.isCanSwitchToSeller())
        .build();
  }

  // 판매자 전용 응답 생성 메서드
  private SellerMyPageSummaryResponse toSellerSummaryResponse(
      UserMyPageSummaryDto dto, EnumResponse userType) {

    return SellerMyPageSummaryResponse.builder()
        .nickname(dto.getNickname())
        .profileImageUrl(dto.getProfileImageUrl())
        .userType(userType)
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
