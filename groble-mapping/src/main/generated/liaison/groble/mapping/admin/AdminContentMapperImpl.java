package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminContentSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-13T16:30:11+0900",
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

    adminContentSummaryInfoResponse.contentId(adminContentSummaryInfoDTO.getContentId());
    adminContentSummaryInfoResponse.createdAt(adminContentSummaryInfoDTO.getCreatedAt());
    adminContentSummaryInfoResponse.contentType(adminContentSummaryInfoDTO.getContentType());
    adminContentSummaryInfoResponse.sellerName(adminContentSummaryInfoDTO.getSellerName());
    adminContentSummaryInfoResponse.contentTitle(adminContentSummaryInfoDTO.getContentTitle());
    adminContentSummaryInfoResponse.minPrice(adminContentSummaryInfoDTO.getMinPrice());
    adminContentSummaryInfoResponse.priceOptionLength(
        adminContentSummaryInfoDTO.getPriceOptionLength());
    adminContentSummaryInfoResponse.contentStatus(adminContentSummaryInfoDTO.getContentStatus());
    adminContentSummaryInfoResponse.adminContentCheckingStatus(
        adminContentSummaryInfoDTO.getAdminContentCheckingStatus());

    return adminContentSummaryInfoResponse.build();
  }
}
