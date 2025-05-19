package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.application.user.dto.UserHeaderDto;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;

@Component
public class UserDtoMapper {

  // 중첩 구조 문제를 해결하기 위해 매퍼 메서드 수정
  public MyPageSummaryResponseBase toApiMyPageSummaryResponse(
      UserMyPageSummaryDto userMyPageSummaryDto) {

    String userTypeName = userMyPageSummaryDto.getUserTypeName();

    // 사용자 유형에 따라 다른 응답 객체 직접 반환 (래퍼 사용하지 않음)
    if (userTypeName != null && userTypeName.equals("SELLER")) {
      return toSellerSummaryResponse(userMyPageSummaryDto, userTypeName);
    } else {
      return toBuyerSummaryResponse(userMyPageSummaryDto, userTypeName);
    }
  }

  // 구매자 전용 응답 생성 메서드
  private BuyerMyPageSummaryResponse toBuyerSummaryResponse(
      UserMyPageSummaryDto dto, String userType) {

    return BuyerMyPageSummaryResponse.builder()
        .nickname(dto.getNickname())
        .profileImageUrl(dto.getProfileImageUrl())
        .userType(userType)
        .canSwitchToSeller(dto.isCanSwitchToSeller())
        .alreadyRegisteredAsSeller(dto.isAlreadyRegisteredAsSeller())
        .build();
  }

  // 판매자 전용 응답 생성 메서드
  private SellerMyPageSummaryResponse toSellerSummaryResponse(
      UserMyPageSummaryDto dto, String userType) {

    return SellerMyPageSummaryResponse.builder()
        .nickname(dto.getNickname())
        .profileImageUrl(dto.getProfileImageUrl())
        .userType(userType)
        .verificationStatus(dto.getVerificationStatusName())
        .alreadyRegisteredAsSeller(dto.isAlreadyRegisteredAsSeller())
        .build();
  }

  public UserMyPageDetailResponse toApiMyPageDetailResponse(
      UserMyPageDetailDto userMyPageDetailDto) {
    String accountType = userMyPageDetailDto.getAccountTypeName();

    String providerType = userMyPageDetailDto.getProviderTypeName();

    return UserMyPageDetailResponse.builder()
        .nickname(userMyPageDetailDto.getNickname())
        .userType(userMyPageDetailDto.getUserTypeName())
        .accountType(accountType)
        .providerType(providerType)
        .email(userMyPageDetailDto.getEmail())
        .profileImageUrl(userMyPageDetailDto.getProfileImageUrl())
        .phoneNumber(userMyPageDetailDto.getPhoneNumber())
        .sellerAccountNotCreated(userMyPageDetailDto.isSellerAccountNotCreated())
        .build();
  }

  public UserHeaderResponse toApiUserHeaderResponse(UserHeaderDto userHeaderDto) {
    return UserHeaderResponse.builder()
        .isLogin(userHeaderDto.getIsLogin())
        .nickname(userHeaderDto.getNickname())
        .profileImageUrl(userHeaderDto.getProfileImageUrl())
        .canSwitchToSeller(userHeaderDto.isCanSwitchToSeller())
        .unreadNotificationCount(userHeaderDto.getUnreadNotificationCount())
        .alreadyRegisteredAsSeller(userHeaderDto.isAlreadyRegisteredAsSeller())
        .build();
  }
}
