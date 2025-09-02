package liaison.groble.application.notification.dto;

import java.math.BigDecimal;

import liaison.groble.application.notification.enums.KakaoNotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoNotificationDTO {
  private KakaoNotificationType type;
  private String username;
  private String sellerName;
  private String buyerName;
  private String phoneNumber;

  // Purchase_Complete_Template
  private String contentTitle;
  private BigDecimal price;
  private String merchantUid;

  // Sale_Complete_Template & Content_Discontinued_Template
  private Long contentId;

  // Review_Registered_Template
  private Long reviewId;

  // Approve_Cancel_Template
  private BigDecimal refundedAmount;
}
