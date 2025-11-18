package liaison.groble.application.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import liaison.groble.application.notification.enums.KakaoNotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoNotificationDTO {
  private KakaoNotificationType type;
  private String username;
  private String sellerName;
  private String rejectionReason;
  private String buyerName;
  private String phoneNumber;
  private String testerNickname;

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

  // Settlement_Completed_Template
  private LocalDate settlementDate;
  private BigDecimal settlementAmount;
  private String contentTypeLabel;

  // Subscription templates
  private LocalDate nextBillingDate;
  private Integer subscriptionRound;
  private LocalDate nextRetryDate; // 다음 재시도 날짜
  private Integer retryCount; // 재시도 횟수
  private LocalDate gracePeriodEndsAt; // 유예기간 종료일
  private String failureReason; // 결제 실패 사유
}
