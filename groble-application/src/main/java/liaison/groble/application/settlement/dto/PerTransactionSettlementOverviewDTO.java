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
  private String orderStatus;
  private LocalDateTime purchasedAt;
  private String paymentType;
}
