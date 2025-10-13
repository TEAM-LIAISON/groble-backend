package liaison.groble.application.dashboard.dto.referrer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
  private String landingPageUrl;
  private String lastPageUrl;
  private List<String> referrerChain;
  private String firstLandingPageUrl;
  private String firstReferrerUrl;
  private String connectionType;
  private Double deviceMemory;
  private Integer hardwareConcurrency;
  private String language;
  private String platform;
  private String screenResolution;
  private String timezone;
  private String userAgent;
  private Map<String, Object> socialAppInfo;
  private Map<String, Object> clientHints;
  private String sessionId;
  private LocalDateTime timestamp;
  private Map<String, Object> referrerDetails;
}
