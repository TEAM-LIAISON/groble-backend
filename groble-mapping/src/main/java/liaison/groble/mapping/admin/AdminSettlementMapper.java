package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.admin.settlement.request.SettlementApprovalRequest;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementDetailResponse;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementsOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.PerTransactionAdminSettlementOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.PgFeeAdjustmentResponse;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.application.admin.settlement.dto.AdminSettlementDetailDTO;
import liaison.groble.application.admin.settlement.dto.AdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.PerTransactionAdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.PgFeeAdjustmentDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminSettlementMapper extends PageResponseMapper {

  /** API 요청을 Application DTO로 변환 */
  SettlementApprovalRequestDTO toRequestDTO(SettlementApprovalRequest request);

  /** Application DTO를 API 응답으로 변환 */
  @Mapping(target = "paypleResult", source = "paypleResult")
  @Mapping(target = "failedSettlements", source = "failedSettlements")
  SettlementApprovalResponse toResponse(SettlementApprovalDTO dto);

  // ====== 📤 PageResponse 변환 ======
  default PageResponse<AdminSettlementsOverviewResponse> toAdminSettlementsOverviewResponsePage(
      PageResponse<AdminSettlementOverviewDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toAdminSettlementsOverviewResponse);
  }

  default PageResponse<PerTransactionAdminSettlementOverviewResponse>
      toPerTransactionAdminSettlementOverviewResponsePage(
          PageResponse<PerTransactionAdminSettlementOverviewDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toPerTransactionAdminSettlementOverviewResponse);
  }

  default PageResponse<PgFeeAdjustmentResponse> toPgFeeAdjustmentResponsePage(
      PageResponse<PgFeeAdjustmentDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toPgFeeAdjustmentResponse);
  }

  AdminSettlementsOverviewResponse toAdminSettlementsOverviewResponse(
      AdminSettlementOverviewDTO adminSettlementOverviewDTO);

  AdminSettlementDetailResponse toAdminSettlementDetailResponse(
      AdminSettlementDetailDTO adminSettlementDetailDTO);

  PerTransactionAdminSettlementOverviewResponse toPerTransactionAdminSettlementOverviewResponse(
      PerTransactionAdminSettlementOverviewDTO perTransactionAdminSettlementOverviewDTO);

  PgFeeAdjustmentResponse toPgFeeAdjustmentResponse(PgFeeAdjustmentDTO dto);

  /** PaypleSettlementResultDTO 매핑 */
  SettlementApprovalResponse.PaypleSettlementResult toPaypleResult(
      SettlementApprovalDTO.PaypleSettlementResultDTO dto);

  /** FailedSettlementDTO 매핑 */
  SettlementApprovalResponse.FailedSettlement toFailedSettlement(
      SettlementApprovalDTO.FailedSettlementDTO dto);
}
