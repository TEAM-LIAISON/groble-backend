package liaison.groble.domain.settlement.dto;

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
  private String settlementAmount;
  private LocalDateTime purchasedAt;
}
