package liaison.groble.mapping.admin;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.api.model.admin.response.AdminOrderCancellationReasonResponse;
import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDTO;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminOrderMapper {

  // ====== 📤 DTO → Response 변환 ======
  AdminOrderCancelRequestResponse toAdminOrderCancelRequestResponse(
      AdminOrderCancelRequestDTO adminOrderCancelRequestDTO);

  AdminOrderCancellationReasonResponse toAdminOrderCancellationReasonResponse(
      AdminOrderCancellationReasonDTO adminOrderCancellationReasonDTO);

  default PageResponse<AdminOrderSummaryInfoResponse> toAdminOrderSummaryInfoResponsePage(
      PageResponse<AdminOrderSummaryInfoDTO> dtoPage) {
    List<AdminOrderSummaryInfoResponse> items =
        dtoPage.getItems().stream().map(this::toAdminOrderSummaryInfoResponse).toList();

    return PageResponse.<AdminOrderSummaryInfoResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }

  AdminOrderSummaryInfoResponse toAdminOrderSummaryInfoResponse(
      AdminOrderSummaryInfoDTO adminOrderSummaryInfoDTO);
}
