package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleAuthResponseDto {
  private String clientKey;
  private String authKey;
  private String returnUrl;
  private String result;
  private String resultMsg;
}
