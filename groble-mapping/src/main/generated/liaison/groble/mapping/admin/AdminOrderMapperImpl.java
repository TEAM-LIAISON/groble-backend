package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.api.model.admin.response.AdminOrderCancellationReasonResponse;
import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDTO;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-16T02:08:23+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminOrderMapperImpl implements AdminOrderMapper {

  @Override
  public AdminOrderCancelRequestResponse toAdminOrderCancelRequestResponse(
      AdminOrderCancelRequestDTO dto) {
    if (dto == null) {
      return null;
    }

    AdminOrderCancelRequestResponse.AdminOrderCancelRequestResponseBuilder
        adminOrderCancelRequestResponse = AdminOrderCancelRequestResponse.builder();

    return adminOrderCancelRequestResponse.build();
  }

  @Override
  public AdminOrderCancellationReasonResponse toAdminOrderCancellationReasonResponse(
      AdminOrderCancellationReasonDTO dto) {
    if (dto == null) {
      return null;
    }

    AdminOrderCancellationReasonResponse.AdminOrderCancellationReasonResponseBuilder
        adminOrderCancellationReasonResponse = AdminOrderCancellationReasonResponse.builder();

    if (dto.getCancelReason() != null) {
      adminOrderCancellationReasonResponse.cancelReason(dto.getCancelReason());
    }

    return adminOrderCancellationReasonResponse.build();
  }

  @Override
  public AdminOrderSummaryInfoResponse toAdminOrderSummaryInfoResponse(
      AdminOrderSummaryInfoDTO dto) {
    if (dto == null) {
      return null;
    }

    AdminOrderSummaryInfoResponse.AdminOrderSummaryInfoResponseBuilder
        adminOrderSummaryInfoResponse = AdminOrderSummaryInfoResponse.builder();

    if (dto.getContentId() != null) {
      adminOrderSummaryInfoResponse.contentId(dto.getContentId());
    }
    if (dto.getMerchantUid() != null) {
      adminOrderSummaryInfoResponse.merchantUid(dto.getMerchantUid());
    }
    if (dto.getCreatedAt() != null) {
      adminOrderSummaryInfoResponse.createdAt(dto.getCreatedAt());
    }
    if (dto.getContentType() != null) {
      adminOrderSummaryInfoResponse.contentType(dto.getContentType());
    }
    if (dto.getContentStatus() != null) {
      adminOrderSummaryInfoResponse.contentStatus(dto.getContentStatus());
    }
    if (dto.getPurchaserName() != null) {
      adminOrderSummaryInfoResponse.purchaserName(dto.getPurchaserName());
    }
    if (dto.getContentTitle() != null) {
      adminOrderSummaryInfoResponse.contentTitle(dto.getContentTitle());
    }
    if (dto.getFinalPrice() != null) {
      adminOrderSummaryInfoResponse.finalPrice(dto.getFinalPrice());
    }
    if (dto.getOrderStatus() != null) {
      adminOrderSummaryInfoResponse.orderStatus(dto.getOrderStatus());
    }

    return adminOrderSummaryInfoResponse.build();
  }
}
