package liaison.groble.application.settlement.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerTransactionSettlementOverviewDTO {
  private String contentTitle;
  private String settlementAmount;
  private LocalDateTime purchasedAt;
}
