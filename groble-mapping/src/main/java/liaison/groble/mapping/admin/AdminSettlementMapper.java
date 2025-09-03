package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.admin.settlement.request.SettlementApprovalRequest;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminSettlementMapper {

  /** API 요청을 Application DTO로 변환 */
  SettlementApprovalRequestDTO toRequestDTO(SettlementApprovalRequest request);

  /** Application DTO를 API 응답으로 변환 */
  @Mapping(target = "paypleResult", source = "paypleResult")
  @Mapping(target = "failedSettlements", source = "failedSettlements")
  SettlementApprovalResponse toResponse(SettlementApprovalDTO dto);

  /** PaypleSettlementResultDTO 매핑 */
  SettlementApprovalResponse.PaypleSettlementResult toPaypleResult(
      SettlementApprovalDTO.PaypleSettlementResultDTO dto);

  /** FailedSettlementDTO 매핑 */
  SettlementApprovalResponse.FailedSettlement toFailedSettlement(
      SettlementApprovalDTO.FailedSettlementDTO dto);
}
