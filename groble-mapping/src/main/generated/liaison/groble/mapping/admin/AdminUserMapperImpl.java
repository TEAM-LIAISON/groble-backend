package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-17T03:54:09+0900",
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
    if (dto.getCreatedAt() != null) {
      adminUserSummaryInfoResponse.createdAt(dto.getCreatedAt());
    }
    if (dto.getNickname() != null) {
      adminUserSummaryInfoResponse.nickname(dto.getNickname());
    }
    if (dto.getEmail() != null) {
      adminUserSummaryInfoResponse.email(dto.getEmail());
    }
    if (dto.getPhoneNumber() != null) {
      adminUserSummaryInfoResponse.phoneNumber(dto.getPhoneNumber());
    }
    if (dto.getVerificationStatus() != null) {
      adminUserSummaryInfoResponse.verificationStatus(dto.getVerificationStatus());
    }
    if (dto.getBusinessType() != null) {
      adminUserSummaryInfoResponse.businessType(dto.getBusinessType());
    }

    return adminUserSummaryInfoResponse.build();
  }
}
