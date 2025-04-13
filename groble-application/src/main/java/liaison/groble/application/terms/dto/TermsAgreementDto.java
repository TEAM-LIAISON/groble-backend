package liaison.groble.application.terms.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementDto {

  private Long id;
  private Long userId;
  private List<String> termsTypeStrings;
  private String typeString;
  private String title;
  private String version;
  private boolean required;
  private String contentUrl;
  private boolean agreed;
  private LocalDateTime agreedAt;
  private String ipAddress;
  private String userAgent;
  private LocalDateTime effectiveFrom;
  private LocalDateTime effectiveTo;
}
