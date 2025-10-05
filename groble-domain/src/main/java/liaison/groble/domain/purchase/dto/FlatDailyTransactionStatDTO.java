package liaison.groble.domain.purchase.dto;

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
public class FlatDailyTransactionStatDTO {
  private LocalDate date;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
}
