package liaison.groble.application.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoNotificationDTO {
  private String userName;
  private String sellerName;
  private String buyerName;
}
