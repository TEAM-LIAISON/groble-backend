package liaison.groble.api.model.admin.dashboard.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardTopContentResponse {
  private Long contentId;
  private String contentTitle;
  private Long sellerId;
  private String sellerNickname;
  private BigDecimal totalRevenue;
  private Long totalSalesCount;
  private BigDecimal averageOrderValue;
}
