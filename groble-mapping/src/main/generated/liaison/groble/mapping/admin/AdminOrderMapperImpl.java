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
    date = "2025-07-12T17:14:39+0900",
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

    adminOrderCancellationReasonResponse.cancelReason(dto.getCancelReason());

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

    adminOrderSummaryInfoResponse.contentId(dto.getContentId());
    adminOrderSummaryInfoResponse.merchantUid(dto.getMerchantUid());
    adminOrderSummaryInfoResponse.createdAt(dto.getCreatedAt());
    adminOrderSummaryInfoResponse.contentType(dto.getContentType());
    adminOrderSummaryInfoResponse.contentStatus(dto.getContentStatus());
    adminOrderSummaryInfoResponse.purchaserName(dto.getPurchaserName());
    adminOrderSummaryInfoResponse.contentTitle(dto.getContentTitle());
    adminOrderSummaryInfoResponse.finalPrice(dto.getFinalPrice());
    adminOrderSummaryInfoResponse.orderStatus(dto.getOrderStatus());

    return adminOrderSummaryInfoResponse.build();
  }
}
