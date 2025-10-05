package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.admin.request.AdminBusinessInfoUpdateRequest;
import liaison.groble.api.model.admin.response.AdminAccountVerificationResponse;
import liaison.groble.api.model.admin.response.AdminGuestUserSummaryResponse;
import liaison.groble.api.model.admin.response.AdminHomeTestContactResponse;
import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminAccountVerificationResultDTO;
import liaison.groble.application.admin.dto.AdminBusinessInfoUpdateDTO;
import liaison.groble.application.admin.dto.AdminGuestUserSummaryDTO;
import liaison.groble.application.admin.dto.AdminHomeTestContactDTO;
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
}
