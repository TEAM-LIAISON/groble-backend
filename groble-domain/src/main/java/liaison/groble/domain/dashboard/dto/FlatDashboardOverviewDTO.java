package liaison.groble.domain.dashboard.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatDashboardOverviewDTO {
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
  private BigDecimal currentMonthRevenue;
  private Long currentMonthSalesCount;
  private Long totalCustomers;
  private Long recentCustomers;
}
