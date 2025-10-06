package liaison.groble.application.admin.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardTrendDTO {
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
  private BigDecimal averageOrderValue;
  private List<AdminDashboardTrendPointDTO> daily; // 일별 지표 목록
}
