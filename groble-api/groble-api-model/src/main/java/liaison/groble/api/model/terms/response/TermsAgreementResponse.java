package liaison.groble.api.model.terms.response;

import java.time.Instant;
import java.time.LocalDateTime;

import liaison.groble.api.model.terms.enums.TermsTypeDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementResponse {

  private Long id;
  private TermsTypeDto type;
  private String title;
  private String version;
  private boolean required;
  private String contentUrl;
  private boolean agreed;
  private Instant agreedAt;
  private LocalDateTime effectiveFrom;
  private LocalDateTime effectiveTo;
}
