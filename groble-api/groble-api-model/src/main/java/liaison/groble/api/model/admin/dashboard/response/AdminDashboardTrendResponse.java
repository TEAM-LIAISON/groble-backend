package liaison.groble.api.model.admin.dashboard.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardTrendResponse {
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
  private BigDecimal averageOrderValue;
  private List<DailyMetricResponse> daily;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DailyMetricResponse {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private BigDecimal totalRevenue;
    private Long totalSalesCount;
    private BigDecimal averageOrderValue;
  }
}
