package liaison.groble.domain.dashboard.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatMarketViewStatsDTO {
  private LocalDate viewDate;
  private String dayOfWeek;
  private Long viewCount;
}
