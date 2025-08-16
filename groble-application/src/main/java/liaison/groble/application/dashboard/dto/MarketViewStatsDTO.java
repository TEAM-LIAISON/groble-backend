package liaison.groble.application.dashboard.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketViewStatsDTO {
  private LocalDate viewDate;
  private String dayOfWeek;
  private Long viewCount;
}
