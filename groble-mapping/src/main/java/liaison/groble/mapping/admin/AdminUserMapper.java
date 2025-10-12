package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.admin.request.AdminBusinessInfoUpdateRequest;
import liaison.groble.api.model.admin.response.AdminAccountVerificationResponse;
import liaison.groble.api.model.admin.response.AdminGuestUserSummaryResponse;
import liaison.groble.api.model.admin.response.AdminHomeTestContactResponse;
import liaison.groble.api.model.admin.response.AdminUserStatisticsResponse;
import liaison.groble.api.model.admin.response.AdminUserStatisticsResponse.BusinessTypeStats;
import liaison.groble.api.model.admin.response.AdminUserStatisticsResponse.VerificationStats;
import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminAccountVerificationResultDTO;
import liaison.groble.application.admin.dto.AdminBusinessInfoUpdateDTO;
import liaison.groble.application.admin.dto.AdminGuestUserSummaryDTO;
import liaison.groble.application.admin.dto.AdminHomeTestContactDTO;
import liaison.groble.application.admin.dto.AdminUserStatisticsDTO;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminUserMapper extends PageResponseMapper {
  @Mapping(target = "isSellerTermsAgreed", source = "sellerTermsAgreed")
  @Mapping(target = "isMarketingAgreed", source = "marketingAgreed")
  @Mapping(target = "isSellerInfo", source = "sellerInfo")
  @Mapping(target = "isBusinessSeller", source = "businessSeller")
  AdminUserSummaryInfoResponse toAdminUserSummaryInfoResponse(AdminUserSummaryInfoDTO dto);

  /* 페이지 변환 – default 메서드는 수동 구현 + 재사용 */
  default PageResponse<AdminUserSummaryInfoResponse> toAdminUserSummaryInfoResponsePage(
      PageResponse<AdminUserSummaryInfoDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toAdminUserSummaryInfoResponse);
  }

  AdminGuestUserSummaryResponse toAdminGuestUserSummaryResponse(AdminGuestUserSummaryDTO dto);

  default PageResponse<AdminGuestUserSummaryResponse> toAdminGuestUserSummaryResponsePage(
      PageResponse<AdminGuestUserSummaryDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toAdminGuestUserSummaryResponse);
  }

  AdminHomeTestContactResponse toAdminHomeTestContactResponse(AdminHomeTestContactDTO dto);

  default PageResponse<AdminHomeTestContactResponse> toAdminHomeTestContactResponsePage(
      PageResponse<AdminHomeTestContactDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toAdminHomeTestContactResponse);
  }

  AdminAccountVerificationResponse toAdminAccountVerificationResponse(
      AdminAccountVerificationResultDTO dto);

  AdminBusinessInfoUpdateDTO toAdminBusinessInfoUpdateDTO(AdminBusinessInfoUpdateRequest request);

  default AdminUserStatisticsResponse toAdminUserStatisticsResponse(AdminUserStatisticsDTO dto) {
    if (dto == null) {
      return null;
    }

    AdminUserStatisticsDTO.VerificationStats verificationStatsDto = dto.getVerificationStats();
    VerificationStats verificationStats =
        VerificationStats.builder()
            .verified(verificationStatsDto != null ? verificationStatsDto.getVerified() : 0)
            .pending(verificationStatsDto != null ? verificationStatsDto.getPending() : 0)
            .inProgress(verificationStatsDto != null ? verificationStatsDto.getInProgress() : 0)
            .failed(verificationStatsDto != null ? verificationStatsDto.getFailed() : 0)
            .none(verificationStatsDto != null ? verificationStatsDto.getNone() : 0)
            .build();

    AdminUserStatisticsDTO.BusinessTypeStats businessTypeStatsDto = dto.getBusinessTypeStats();
    BusinessTypeStats businessTypeStats =
        BusinessTypeStats.builder()
            .individualSimplified(
                businessTypeStatsDto != null ? businessTypeStatsDto.getIndividualSimplified() : 0)
            .individualNormal(
                businessTypeStatsDto != null ? businessTypeStatsDto.getIndividualNormal() : 0)
            .corporate(businessTypeStatsDto != null ? businessTypeStatsDto.getCorporate() : 0)
            .none(businessTypeStatsDto != null ? businessTypeStatsDto.getNone() : 0)
            .build();

    return AdminUserStatisticsResponse.builder()
        .totalUsers(dto.getTotalUsers())
        .withdrawnUsers(dto.getWithdrawnUsers())
        .newUsers7Days(dto.getNewUsers7Days())
        .newUsers30Days(dto.getNewUsers30Days())
        .buyerOnlyCount(dto.getBuyerOnlyCount())
        .buyerAndSellerCount(dto.getBuyerAndSellerCount())
        .buyerOnlyPercentage(dto.getBuyerOnlyPercentage())
        .buyerAndSellerPercentage(dto.getBuyerAndSellerPercentage())
        .marketingAgreedCount(dto.getMarketingAgreedCount())
        .marketingAgreedPercentage(dto.getMarketingAgreedPercentage())
        .phoneNumberProvidedCount(dto.getPhoneNumberProvidedCount())
        .phoneNumberProvidedPercentage(dto.getPhoneNumberProvidedPercentage())
        .phoneNumberNotProvidedCount(dto.getPhoneNumberNotProvidedCount())
        .phoneNumberNotProvidedPercentage(dto.getPhoneNumberNotProvidedPercentage())
        .sellerTermsAgreedCount(dto.getSellerTermsAgreedCount())
        .sellerTermsAgreedPercentage(dto.getSellerTermsAgreedPercentage())
        .verificationStats(verificationStats)
        .verificationSuccessRate(dto.getVerificationSuccessRate())
        .businessTypeStats(businessTypeStats)
        .build();
  }
}
