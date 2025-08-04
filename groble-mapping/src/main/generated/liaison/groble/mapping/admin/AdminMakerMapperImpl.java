package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminMakerDetailInfoResponse;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-04T18:38:19+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminMakerMapperImpl implements AdminMakerMapper {

  @Override
  public AdminMakerDetailInfoResponse toAdminMakerDetailInfoResponse(
      AdminMakerDetailInfoDTO adminMakerDetailInfoDTO) {
    if (adminMakerDetailInfoDTO == null) {
      return null;
    }

    AdminMakerDetailInfoResponse.AdminMakerDetailInfoResponseBuilder adminMakerDetailInfoResponse =
        AdminMakerDetailInfoResponse.builder();

    if (adminMakerDetailInfoDTO.getIsBusinessMaker() != null) {
      adminMakerDetailInfoResponse.isBusinessMaker(adminMakerDetailInfoDTO.getIsBusinessMaker());
    }
    if (adminMakerDetailInfoDTO.getVerificationStatus() != null) {
      adminMakerDetailInfoResponse.verificationStatus(
          adminMakerDetailInfoDTO.getVerificationStatus());
    }
    if (adminMakerDetailInfoDTO.getBankAccountOwner() != null) {
      adminMakerDetailInfoResponse.bankAccountOwner(adminMakerDetailInfoDTO.getBankAccountOwner());
    }
    if (adminMakerDetailInfoDTO.getBankName() != null) {
      adminMakerDetailInfoResponse.bankName(adminMakerDetailInfoDTO.getBankName());
    }
    if (adminMakerDetailInfoDTO.getBankAccountNumber() != null) {
      adminMakerDetailInfoResponse.bankAccountNumber(
          adminMakerDetailInfoDTO.getBankAccountNumber());
    }
    if (adminMakerDetailInfoDTO.getCopyOfBankBookOriginalFileName() != null) {
      adminMakerDetailInfoResponse.copyOfBankBookOriginalFileName(
          adminMakerDetailInfoDTO.getCopyOfBankBookOriginalFileName());
    }
    if (adminMakerDetailInfoDTO.getCopyOfBankbookUrl() != null) {
      adminMakerDetailInfoResponse.copyOfBankbookUrl(
          adminMakerDetailInfoDTO.getCopyOfBankbookUrl());
    }
    if (adminMakerDetailInfoDTO.getBusinessType() != null) {
      adminMakerDetailInfoResponse.businessType(adminMakerDetailInfoDTO.getBusinessType());
    }
    if (adminMakerDetailInfoDTO.getBusinessCategory() != null) {
      adminMakerDetailInfoResponse.businessCategory(adminMakerDetailInfoDTO.getBusinessCategory());
    }
    if (adminMakerDetailInfoDTO.getBusinessSector() != null) {
      adminMakerDetailInfoResponse.businessSector(adminMakerDetailInfoDTO.getBusinessSector());
    }
    if (adminMakerDetailInfoDTO.getBusinessName() != null) {
      adminMakerDetailInfoResponse.businessName(adminMakerDetailInfoDTO.getBusinessName());
    }
    if (adminMakerDetailInfoDTO.getRepresentativeName() != null) {
      adminMakerDetailInfoResponse.representativeName(
          adminMakerDetailInfoDTO.getRepresentativeName());
    }
    if (adminMakerDetailInfoDTO.getBusinessAddress() != null) {
      adminMakerDetailInfoResponse.businessAddress(adminMakerDetailInfoDTO.getBusinessAddress());
    }
    if (adminMakerDetailInfoDTO.getBusinessLicenseOriginalFileName() != null) {
      adminMakerDetailInfoResponse.businessLicenseOriginalFileName(
          adminMakerDetailInfoDTO.getBusinessLicenseOriginalFileName());
    }
    if (adminMakerDetailInfoDTO.getBusinessLicenseFileUrl() != null) {
      adminMakerDetailInfoResponse.businessLicenseFileUrl(
          adminMakerDetailInfoDTO.getBusinessLicenseFileUrl());
    }
    if (adminMakerDetailInfoDTO.getTaxInvoiceEmail() != null) {
      adminMakerDetailInfoResponse.taxInvoiceEmail(adminMakerDetailInfoDTO.getTaxInvoiceEmail());
    }

    return adminMakerDetailInfoResponse.build();
  }
}
