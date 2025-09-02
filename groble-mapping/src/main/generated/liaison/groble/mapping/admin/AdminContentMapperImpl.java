package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminContentSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-02T17:55:20+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminContentMapperImpl implements AdminContentMapper {

  @Override
  public AdminContentSummaryInfoResponse toAdminContentSummaryInfoResponse(
      AdminContentSummaryInfoDTO adminContentSummaryInfoDTO) {
    if (adminContentSummaryInfoDTO == null) {
      return null;
    }

    AdminContentSummaryInfoResponse.AdminContentSummaryInfoResponseBuilder
        adminContentSummaryInfoResponse = AdminContentSummaryInfoResponse.builder();

    if (adminContentSummaryInfoDTO.getContentId() != null) {
      adminContentSummaryInfoResponse.contentId(adminContentSummaryInfoDTO.getContentId());
    }
    if (adminContentSummaryInfoDTO.getCreatedAt() != null) {
      adminContentSummaryInfoResponse.createdAt(adminContentSummaryInfoDTO.getCreatedAt());
    }
    if (adminContentSummaryInfoDTO.getContentType() != null) {
      adminContentSummaryInfoResponse.contentType(adminContentSummaryInfoDTO.getContentType());
    }
    if (adminContentSummaryInfoDTO.getSellerName() != null) {
      adminContentSummaryInfoResponse.sellerName(adminContentSummaryInfoDTO.getSellerName());
    }
    if (adminContentSummaryInfoDTO.getContentTitle() != null) {
      adminContentSummaryInfoResponse.contentTitle(adminContentSummaryInfoDTO.getContentTitle());
    }
    if (adminContentSummaryInfoDTO.getMinPrice() != null) {
      adminContentSummaryInfoResponse.minPrice(adminContentSummaryInfoDTO.getMinPrice());
    }
    adminContentSummaryInfoResponse.priceOptionLength(
        adminContentSummaryInfoDTO.getPriceOptionLength());
    if (adminContentSummaryInfoDTO.getContentStatus() != null) {
      adminContentSummaryInfoResponse.contentStatus(adminContentSummaryInfoDTO.getContentStatus());
    }
    if (adminContentSummaryInfoDTO.getAdminContentCheckingStatus() != null) {
      adminContentSummaryInfoResponse.adminContentCheckingStatus(
          adminContentSummaryInfoDTO.getAdminContentCheckingStatus());
    }

    return adminContentSummaryInfoResponse.build();
  }
}
