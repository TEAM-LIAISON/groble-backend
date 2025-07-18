package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;

@Component
public class UserDTOMapper {

  // 중첩 구조 문제를 해결하기 위해 매퍼 메서드 수정
  public MyPageSummaryResponseBase toApiMyPageSummaryResponse(
      UserMyPageSummaryDTO userMyPageSummaryDTO) {

    String userTypeName = userMyPageSummaryDTO.getUserTypeName();

    // 사용자 유형에 따라 다른 응답 객체 직접 반환 (래퍼 사용하지 않음)
    if (userTypeName != null && userTypeName.equals("SELLER")) {
      return toSellerSummaryResponse(userMyPageSummaryDTO, userTypeName);
    } else {
      return toBuyerSummaryResponse(userMyPageSummaryDTO, userTypeName);
    }
  }

  // 구매자 전용 응답 생성 메서드
  private BuyerMyPageSummaryResponse toBuyerSummaryResponse(
      UserMyPageSummaryDTO dto, String userType) {

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
      UserMyPageSummaryDTO dto, String userType) {

    return SellerMyPageSummaryResponse.builder()
        .nickname(dto.getNickname())
        .profileImageUrl(dto.getProfileImageUrl())
        .userType(userType)
        .verificationStatus(dto.getVerificationStatusName())
        .alreadyRegisteredAsSeller(dto.isAlreadyRegisteredAsSeller())
        .build();
  }

  public UserMyPageDetailResponse toApiMyPageDetailResponse(
      UserMyPageDetailDTO userMyPageDetailDTO) {
    String accountType = userMyPageDetailDTO.getAccountTypeName();

    String providerType = userMyPageDetailDTO.getProviderTypeName();

    return UserMyPageDetailResponse.builder()
        .nickname(userMyPageDetailDTO.getNickname())
        .userType(userMyPageDetailDTO.getUserTypeName())
        .accountType(accountType)
        .providerType(providerType)
        .email(userMyPageDetailDTO.getEmail())
        .profileImageUrl(userMyPageDetailDTO.getProfileImageUrl())
        .phoneNumber(userMyPageDetailDTO.getPhoneNumber())
        .canSwitchToSeller(userMyPageDetailDTO.isCanSwitchToSeller())
        .sellerAccountNotCreated(userMyPageDetailDTO.isSellerAccountNotCreated())
        .verificationStatus(userMyPageDetailDTO.getVerificationStatus())
        .sellerAccountNotCreated(userMyPageDetailDTO.isAlreadyRegisteredAsSeller())
        .build();
  }

  public UserHeaderResponse toApiUserHeaderResponse(UserHeaderDTO userHeaderDTO) {
    return UserHeaderResponse.builder()
        .isLogin(userHeaderDTO.getIsLogin())
        .nickname(userHeaderDTO.getNickname())
        .profileImageUrl(userHeaderDTO.getProfileImageUrl())
        .canSwitchToSeller(userHeaderDTO.isCanSwitchToSeller())
        .unreadNotificationCount(userHeaderDTO.getUnreadNotificationCount())
        .alreadyRegisteredAsSeller(userHeaderDTO.isAlreadyRegisteredAsSeller())
        .lastUserType(userHeaderDTO.getLastUserType())
        .build();
  }
}
