package liaison.groble.application.dashboard.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardOverviewDTO {
  private String verificationStatus;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
  private BigDecimal currentMonthRevenue;
  private Long currentMonthSalesCount;
  private Long totalMarketViews;
  private Long totalContentViews;
  private Long totalCustomerViews;
  private Long recentCustomerViews;
}
