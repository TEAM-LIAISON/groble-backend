package liaison.groble.application.settlement.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementOverviewDTO {
  private String verificationStatus;
  private BigDecimal totalSettlementAmount;
  private BigDecimal currentMonthSettlementAmount;
}
