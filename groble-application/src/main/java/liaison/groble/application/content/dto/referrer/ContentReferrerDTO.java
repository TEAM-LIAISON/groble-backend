package liaison.groble.application.content.dto.referrer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentReferrerDTO {
  private String pageUrl;
  private String referrerUrl;
  private String utmSource;
  private String utmMedium;
  private String utmCampaign;
  private String utmContent;
  private String utmTerm;
}
