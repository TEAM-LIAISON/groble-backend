package liaison.groble.domain.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatSettlementsDTO {
  private Long settlementId;
  private LocalDate settlementStartDate;
  private LocalDate settlementEndDate;
  private LocalDate scheduledSettlementDate;
  private String contentType;
  private BigDecimal settlementAmount;
  private BigDecimal settlementAmountDisplay;
  private String settlementStatus;
}
