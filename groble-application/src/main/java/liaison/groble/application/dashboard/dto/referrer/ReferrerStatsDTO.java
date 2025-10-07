package liaison.groble.application.dashboard.dto.referrer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReferrerStatsDTO {
  private String referrerUrl;
  private String referrerDomain;
  private String referrerPath;
  private String source;
  private String medium;
  private String campaign;
  private String content;
  private String term;
  private String trafficType;
  private boolean external;
  private String displayLabel;
  private Long visitCount;
}
