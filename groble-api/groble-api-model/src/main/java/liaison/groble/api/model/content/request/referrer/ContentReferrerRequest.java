package liaison.groble.api.model.content.request.referrer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReferrerRequest {
  private String pageUrl;
  private String referrerUrl;
  private String utmSource;
  private String utmMedium;
  private String utmCampaign;
  private String utmContent;
  private String utmTerm;
}
