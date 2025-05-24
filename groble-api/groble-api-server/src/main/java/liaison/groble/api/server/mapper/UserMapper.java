package liaison.groble.api.server.mapper;

/*
 * 임시로 주석 처리 - OpenAPI 생성 후 활성화 예정
 *

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  @Mapping(target = "userType", expression = "java(mapUserType(dto.getUserType()))")
  MyPageSummaryResponse toMyPageSummaryResponse(UserMyPageSummaryDto dto);

  default MyPageSummaryResponse toMyPageSummaryResponse(UserMyPageSummaryDto dto) {
    if (dto == null) {
      return null;
    }

    if ("BUYER".equals(dto.getUserType())) {
      return BuyerMyPageSummary.builder()
          .nickname(dto.getNickname())
          .profileImageUrl(dto.getProfileImageUrl())
          .userType(UserType.BUYER)
          .canSwitchToSeller(dto.isCanSwitchToSeller())
          .build();
    } else {
      return SellerMyPageSummary.builder()
          .nickname(dto.getNickname())
          .profileImageUrl(dto.getProfileImageUrl())
          .userType(UserType.SELLER)
          .verificationStatus(mapVerificationStatus(dto.getVerificationStatus()))
          .build();
    }
  }

  default UserType mapUserType(String userType) {
    return UserType.valueOf(userType);
  }

  @Mapping(source = "code", target = "code")
  @Mapping(source = "description", target = "description")
  VerificationStatus mapVerificationStatus(VerificationStatusDto dto);

  @Mapping(source = "userType", target = "targetUserType")
  UserTypeSwitchDto toUserTypeSwitchDto(UserTypeRequest request);

  List<UserHeaderResponse> toUserHeaderResponseList(List<UserHeaderDto> dtos);
}
*/
