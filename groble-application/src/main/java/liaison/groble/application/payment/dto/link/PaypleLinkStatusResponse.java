package liaison.groble.application.payment.dto.link;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleLinkStatusResponse {
  private String merchantUid;
  private String status;
  private String linkUrl;
  private LocalDateTime createdAt;
  private LocalDateTime expireAt;
  private String paymentStatus;
  private String paymentMessage;
  private LocalDateTime paymentAt;
}
