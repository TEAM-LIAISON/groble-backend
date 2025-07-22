package liaison.groble.mapping.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.user.request.SetSocialBasicInfoRequest;
import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface UserMapper {

  // ====== 📥 Request → DTO 변환 ======
  @Mapping(
      target = "termsTypeStrings",
      expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
  SocialBasicInfoDTO toSocialBasicInfoDTO(SetSocialBasicInfoRequest request);

  UserHeaderResponse toUserHeaderResponse(UserHeaderDTO userHeaderDTO);

  // ====== 📤 DTO → Response 변환 ======

  /** 사용자 유형에 따라 다른 응답 객체를 반환하는 메서드 */

  /** 사용자 유형에 따라 다른 응답 객체를 반환하는 메서드 */
  default MyPageSummaryResponseBase toApiMyPageSummaryResponse(
      UserMyPageSummaryDTO userMyPageSummaryDTO) {
    if (userMyPageSummaryDTO == null) {
      return null;
    }

    String userTypeName = userMyPageSummaryDTO.getUserTypeName();

    if ("SELLER".equals(userTypeName)) {
      return toSellerSummaryResponse(userMyPageSummaryDTO);
    } else {
      return toBuyerSummaryResponse(userMyPageSummaryDTO);
    }
  }

  /** 구매자 전용 응답 생성 */
  @Mapping(target = "userType", source = "userTypeName")
  @Mapping(target = "canSwitchToSeller", source = "canSwitchToSeller")
  @Mapping(target = "alreadyRegisteredAsSeller", source = "alreadyRegisteredAsSeller")
  BuyerMyPageSummaryResponse toBuyerSummaryResponse(UserMyPageSummaryDTO userMyPageSummaryDTO);

  /** 판매자 전용 응답 생성 */
  @Mapping(target = "userType", source = "userTypeName")
  @Mapping(target = "verificationStatus", source = "verificationStatusName")
  @Mapping(target = "alreadyRegisteredAsSeller", source = "alreadyRegisteredAsSeller")
  SellerMyPageSummaryResponse toSellerSummaryResponse(UserMyPageSummaryDTO userMyPageSummaryDTO);

  /** 사용자 마이페이지 상세 정보 변환 */
  @Mapping(target = "userType", source = "userTypeName")
  @Mapping(target = "accountType", source = "accountTypeName")
  @Mapping(target = "providerType", source = "providerTypeName")
  UserMyPageDetailResponse toApiMyPageDetailResponse(UserMyPageDetailDTO userMyPageDetailDTO);
}
