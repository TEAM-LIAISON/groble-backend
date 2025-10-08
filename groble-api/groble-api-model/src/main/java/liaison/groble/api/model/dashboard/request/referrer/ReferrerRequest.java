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

  @NotBlank
  @Size(max = 255)
  private String sessionId;

  @NotBlank private String timestamp;

  private Map<String, Object> referrerDetails;
}
