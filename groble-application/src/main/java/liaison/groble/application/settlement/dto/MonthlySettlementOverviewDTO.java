package liaison.groble.application.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlySettlementOverviewDTO {
  private LocalDate settlementStartDate;
  private LocalDate settlementEndDate;
  private BigDecimal settlementAmount;
  private String settlementStatus;
}
