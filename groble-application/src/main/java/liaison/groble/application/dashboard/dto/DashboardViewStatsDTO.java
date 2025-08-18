package liaison.groble.application.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardViewStatsDTO {
  private Long totalMarketViews;
  private Long totalContentViews;
}
