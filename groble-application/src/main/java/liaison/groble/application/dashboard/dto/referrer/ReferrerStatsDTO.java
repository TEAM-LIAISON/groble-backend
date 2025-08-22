package liaison.groble.application.dashboard.dto.referrer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReferrerStatsDTO {
  private String referrerUrl;
  private Long visitCount;
}
