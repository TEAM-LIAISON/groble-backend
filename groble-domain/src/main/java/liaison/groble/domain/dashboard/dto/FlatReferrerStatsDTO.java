package liaison.groble.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatReferrerStatsDTO {
  private String referrerUrl;
  private String referrerDomain;
  private String referrerPath;
  private String source;
  private String medium;
  private String campaign;
  private String content;
  private String term;
  private Long visitCount;
}
