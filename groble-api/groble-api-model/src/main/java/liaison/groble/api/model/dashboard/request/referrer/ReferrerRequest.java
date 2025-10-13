package liaison.groble.api.model.dashboard.request.referrer;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferrerRequest {

  @NotBlank
  @Size(max = 500)
  private String pageUrl;

  @Size(max = 500)
  private String referrerUrl;

  @Size(max = 255)
  private String utmSource;

  @Size(max = 255)
  private String utmMedium;

  @Size(max = 255)
  private String utmCampaign;

  @Size(max = 255)
  private String utmContent;

  @Size(max = 255)
  private String utmTerm;

  @Size(max = 500)
  private String landingPageUrl;

  @Size(max = 500)
  private String lastPageUrl;

  @Size(max = 20)
  private List<@Size(max = 500) String> referrerChain;

  @Size(max = 50)
  private String connectionType;

  private Double deviceMemory;

  private Integer hardwareConcurrency;

  @Size(max = 50)
  private String language;

  @Size(max = 255)
  private String platform;

  @Size(max = 100)
  private String screenResolution;

  @Size(max = 100)
  private String timezone;

  @Size(max = 500)
  private String userAgent;

  private Map<String, Object> socialAppInfo;

  private Map<String, Object> clientHints;

  @NotBlank
  @Size(max = 255)
  private String sessionId;

  @NotBlank private String timestamp;

  private Map<String, Object> referrerDetails;
}
