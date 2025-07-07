package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T16:52:09+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminOrderMapperImpl implements AdminOrderMapper {

  @Override
  public AdminOrderCancelRequestResponse toAdminOrderCancelRequestResponse(
      AdminOrderCancelRequestDTO adminOrderCancelRequestDTO) {
    if (adminOrderCancelRequestDTO == null) {
      return null;
    }

    AdminOrderCancelRequestResponse.AdminOrderCancelRequestResponseBuilder
        adminOrderCancelRequestResponse = AdminOrderCancelRequestResponse.builder();

    return adminOrderCancelRequestResponse.build();
  }

  @Override
  public AdminOrderSummaryInfoResponse toAdminOrderSummaryInfoResponse(
      AdminOrderSummaryInfoDTO adminOrderSummaryInfoDTO) {
    if (adminOrderSummaryInfoDTO == null) {
      return null;
    }

    AdminOrderSummaryInfoResponse.AdminOrderSummaryInfoResponseBuilder
        adminOrderSummaryInfoResponse = AdminOrderSummaryInfoResponse.builder();

    if (adminOrderSummaryInfoDTO.getContentId() != null) {
      adminOrderSummaryInfoResponse.contentId(adminOrderSummaryInfoDTO.getContentId());
    }
    if (adminOrderSummaryInfoDTO.getMerchantUid() != null) {
      adminOrderSummaryInfoResponse.merchantUid(adminOrderSummaryInfoDTO.getMerchantUid());
    }
    if (adminOrderSummaryInfoDTO.getCreatedAt() != null) {
      adminOrderSummaryInfoResponse.createdAt(adminOrderSummaryInfoDTO.getCreatedAt());
    }
    if (adminOrderSummaryInfoDTO.getContentType() != null) {
      adminOrderSummaryInfoResponse.contentType(adminOrderSummaryInfoDTO.getContentType());
    }
    if (adminOrderSummaryInfoDTO.getContentStatus() != null) {
      adminOrderSummaryInfoResponse.contentStatus(adminOrderSummaryInfoDTO.getContentStatus());
    }
    if (adminOrderSummaryInfoDTO.getPurchaserName() != null) {
      adminOrderSummaryInfoResponse.purchaserName(adminOrderSummaryInfoDTO.getPurchaserName());
    }
    if (adminOrderSummaryInfoDTO.getContentTitle() != null) {
      adminOrderSummaryInfoResponse.contentTitle(adminOrderSummaryInfoDTO.getContentTitle());
    }
    if (adminOrderSummaryInfoDTO.getFinalPrice() != null) {
      adminOrderSummaryInfoResponse.finalPrice(adminOrderSummaryInfoDTO.getFinalPrice());
    }
    if (adminOrderSummaryInfoDTO.getOrderStatus() != null) {
      adminOrderSummaryInfoResponse.orderStatus(adminOrderSummaryInfoDTO.getOrderStatus());
    }

    return adminOrderSummaryInfoResponse.build();
  }
}
