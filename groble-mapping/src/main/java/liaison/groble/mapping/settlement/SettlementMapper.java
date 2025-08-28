package liaison.groble.mapping.settlement;

import org.mapstruct.Mapper;

import liaison.groble.api.model.settlement.response.PerTransactionSettlementOverviewResponse;
import liaison.groble.api.model.settlement.response.SettlementDetailResponse;
import liaison.groble.api.model.settlement.response.SettlementOverviewResponse;
import liaison.groble.api.model.settlement.response.SettlementsOverviewResponse;
import liaison.groble.api.model.settlement.response.TaxInvoiceResponse;
import liaison.groble.application.settlement.dto.PerTransactionSettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.dto.SettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementsOverviewDTO;
import liaison.groble.application.settlement.dto.TaxInvoiceDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface SettlementMapper extends PageResponseMapper {
  // ====== 📤 DTO → Response 변환 ======
  SettlementDetailResponse toSettlementResponse(SettlementDetailDTO settlementDetailDTO);

  SettlementOverviewResponse toSettlementOverviewResponse(
      SettlementOverviewDTO settlementOverviewDTO);

  // ====== 📤 PageResponse 변환 ======
  default PageResponse<SettlementsOverviewResponse> toMonthlySettlementOverviewResponsePage(
      PageResponse<SettlementsOverviewDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toSettlementsOverviewResponse);
  }

  default PageResponse<PerTransactionSettlementOverviewResponse>
      toPerTransactionSettlementOverviewResponsePage(
          PageResponse<PerTransactionSettlementOverviewDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toPerTransactionSettlementOverviewResponse);
  }

  SettlementsOverviewResponse toSettlementsOverviewResponse(
      SettlementsOverviewDTO settlementsOverviewDTO);

  PerTransactionSettlementOverviewResponse toPerTransactionSettlementOverviewResponse(
      PerTransactionSettlementOverviewDTO perTransactionSettlementOverviewDTO);

  TaxInvoiceResponse toTaxInvoiceResponse(TaxInvoiceDTO taxInvoiceDTO);
}
