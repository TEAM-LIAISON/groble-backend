package liaison.groble.application.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerTransactionSettlementOverviewDTO {
  private String contentTitle;
  private BigDecimal settlementAmount;
  private LocalDateTime purchasedAt;
}
