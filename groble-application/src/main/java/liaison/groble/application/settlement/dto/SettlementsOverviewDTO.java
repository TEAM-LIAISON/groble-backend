package liaison.groble.application.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementsOverviewDTO {
  private Long settlementId;
  private LocalDate settlementStartDate;
  private LocalDate settlementEndDate;
  private LocalDate scheduledSettlementDate;
  private String contentType;
  private String paymentType;
  private BigDecimal settlementAmount;
  private String settlementStatus;
}
