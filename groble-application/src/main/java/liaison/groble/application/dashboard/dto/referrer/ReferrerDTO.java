package liaison.groble.application.dashboard.dto.referrer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReferrerDTO {
  private String pageUrl;
  private String referrerUrl;
  private String utmSource;
  private String utmMedium;
  private String utmCampaign;
  private String utmContent;
  private String utmTerm;
}
