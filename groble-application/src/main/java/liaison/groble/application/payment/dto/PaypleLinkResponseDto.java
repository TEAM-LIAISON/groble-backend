package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleLinkResponseDto {
  private String paymentResult;
  private String paymentLinkUrl;
}
