package liaison.groble.mapping.settlement;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.settlement.response.SettlementDetailResponse;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-13T15:04:14+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class SettlementMapperImpl implements SettlementMapper {

  @Override
  public SettlementDetailResponse toSettlementResponse(SettlementDetailDTO settlementDetailDTO) {
    if (settlementDetailDTO == null) {
      return null;
    }

    SettlementDetailResponse.SettlementDetailResponseBuilder settlementDetailResponse =
        SettlementDetailResponse.builder();

    if (settlementDetailDTO.getSettlementStartDate() != null) {
      settlementDetailResponse.settlementStartDate(settlementDetailDTO.getSettlementStartDate());
    }
    if (settlementDetailDTO.getSettlementEndDate() != null) {
      settlementDetailResponse.settlementEndDate(settlementDetailDTO.getSettlementEndDate());
    }
    if (settlementDetailDTO.getScheduledSettlementDate() != null) {
      settlementDetailResponse.scheduledSettlementDate(
          settlementDetailDTO.getScheduledSettlementDate());
    }
    if (settlementDetailDTO.getSettlementAmount() != null) {
      settlementDetailResponse.settlementAmount(settlementDetailDTO.getSettlementAmount());
    }
    if (settlementDetailDTO.getPgFee() != null) {
      settlementDetailResponse.pgFee(settlementDetailDTO.getPgFee());
    }
    if (settlementDetailDTO.getPlatformFee() != null) {
      settlementDetailResponse.platformFee(settlementDetailDTO.getPlatformFee());
    }

    return settlementDetailResponse.build();
  }
}
