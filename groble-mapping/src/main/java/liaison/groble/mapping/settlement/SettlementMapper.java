package liaison.groble.mapping.settlement;

import org.mapstruct.Mapper;

import liaison.groble.api.model.settlement.response.SettlementDetailResponse;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface SettlementMapper {
  // ====== 📤 DTO → Response 변환 ======
  SettlementDetailResponse toSettlementResponse(SettlementDetailDTO settlementDetailDTO);
}
