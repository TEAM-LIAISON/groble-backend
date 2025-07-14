package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.api.model.admin.response.AdminOrderCancellationReasonResponse;
import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDTO;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminOrderMapper extends PageResponseMapper {

  AdminOrderCancelRequestResponse toAdminOrderCancelRequestResponse(AdminOrderCancelRequestDTO dto);

  AdminOrderCancellationReasonResponse toAdminOrderCancellationReasonResponse(
      AdminOrderCancellationReasonDTO dto);

  AdminOrderSummaryInfoResponse toAdminOrderSummaryInfoResponse(AdminOrderSummaryInfoDTO dto);

  default PageResponse<AdminOrderSummaryInfoResponse> toAdminOrderSummaryInfoResponsePage(
      PageResponse<AdminOrderSummaryInfoDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toAdminOrderSummaryInfoResponse);
  }
}
