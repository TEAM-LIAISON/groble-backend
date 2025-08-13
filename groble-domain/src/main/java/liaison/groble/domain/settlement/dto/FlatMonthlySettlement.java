package liaison.groble.domain.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import liaison.groble.domain.settlement.entity.Settlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatMonthlySettlement {
  private LocalDate settlementStartDate;
  private LocalDate settlementEndDate;
  private BigDecimal settlementAmount;
  private Settlement.SettlementStatus settlementStatus;
}
