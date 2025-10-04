package liaison.groble.domain.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatPerTransactionSettlement {
  private String contentTitle;
  private BigDecimal settlementAmount;
  private BigDecimal settlementAmountDisplay;
  private String orderStatus;
  private LocalDateTime purchasedAt;
}
