package liaison.groble.application.settlement.service;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;

import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.domain.settlement.entity.Settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

  private final SettlementReader settlementReader;

  public SettlementDetailDTO getSettlementDetail(Long userId, YearMonth yearMonth) {
    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();
    Settlement settlement =
        settlementReader.getSettlementByUserIdAndPeriod(userId, startDate, endDate);

    return SettlementDetailDTO.builder()
        .settlementStartDate(settlement.getSettlementStartDate())
        .settlementEndDate(settlement.getSettlementEndDate())
        .scheduledSettlementDate(settlement.getScheduledSettlementDate())
        .settlementAmount(settlement.getSettlementAmount())
        .pgFee(settlement.getPgFee())
        .platformFee(settlement.getPlatformFee())
        .build();
  }
}
