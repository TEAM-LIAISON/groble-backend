package liaison.groble.application.admin.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerTransactionAdminSettlementOverviewDTO {
  private String contentTitle;
  private BigDecimal settlementAmount;
  private String orderStatus;
  private LocalDateTime purchasedAt;
}
