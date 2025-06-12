package liaison.groble.application.payment.dto.link;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleLinkResendResponse {
  private String merchantUid;
  private String linkUrl;
  private LocalDateTime sentAt;
  private String method;
  private String targetPhoneNumber;
  private String targetEmail;
  private String resultMessage;
}
