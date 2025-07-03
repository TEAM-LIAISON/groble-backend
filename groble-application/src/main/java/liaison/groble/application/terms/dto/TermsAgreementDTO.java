package liaison.groble.application.terms.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementDTO {

  private Long id;
  private Long userId;
  private List<String> termsTypeStrings;
  private String typeString;
  private String title;
  private String version;
  private boolean required;
  private String contentUrl;
  private boolean agreed;
  private Instant agreedAt;
  private String ipAddress;
  private String userAgent;
  private LocalDateTime effectiveFrom;
  private LocalDateTime effectiveTo;

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }
}
