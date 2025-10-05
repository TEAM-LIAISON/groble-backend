package liaison.groble.application.admin.dashboard.dto;

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
public class AdminDashboardTrendPointDTO {
  private LocalDate date;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
  private BigDecimal averageOrderValue;
}
