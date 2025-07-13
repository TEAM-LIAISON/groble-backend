package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-12T23:01:27+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminUserMapperImpl implements AdminUserMapper {

  @Override
  public AdminUserSummaryInfoResponse toAdminUserSummaryInfoResponse(AdminUserSummaryInfoDTO dto) {
    if (dto == null) {
      return null;
    }

    AdminUserSummaryInfoResponse.AdminUserSummaryInfoResponseBuilder adminUserSummaryInfoResponse =
        AdminUserSummaryInfoResponse.builder();

    adminUserSummaryInfoResponse.isSellerTermsAgreed(dto.isSellerTermsAgreed());
    adminUserSummaryInfoResponse.isMarketingAgreed(dto.isMarketingAgreed());
    adminUserSummaryInfoResponse.isSellerInfo(dto.isSellerInfo());
    adminUserSummaryInfoResponse.isBusinessSeller(dto.isBusinessSeller());
    adminUserSummaryInfoResponse.createdAt(dto.getCreatedAt());
    adminUserSummaryInfoResponse.nickname(dto.getNickname());
    adminUserSummaryInfoResponse.email(dto.getEmail());
    adminUserSummaryInfoResponse.phoneNumber(dto.getPhoneNumber());
    adminUserSummaryInfoResponse.verificationStatus(dto.getVerificationStatus());
    adminUserSummaryInfoResponse.businessType(dto.getBusinessType());

    return adminUserSummaryInfoResponse.build();
  }
}
