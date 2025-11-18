package liaison.groble.domain.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatPurchaseContentDetailDTO {
  private String orderStatus;
  private String merchantUid;
  private Long userId;
  private LocalDateTime purchasedAt;
  private LocalDateTime cancelRequestedAt;
  private LocalDateTime cancelledAt;
  private Long contentId;
  private String sellerName;
  private String contentTitle;
  private String selectedOptionName;
  private Integer selectedOptionQuantity;
  private String selectedOptionType;
  private String documentOptionActionUrl;
  private Boolean isFreePurchase;
  private BigDecimal originalPrice;
  private BigDecimal discountPrice;
  private BigDecimal finalPrice;
  private String payType;
  private String payCardName;
  private String payCardNum;
  private String thumbnailUrl;
  private Boolean isRefundable;
  private String cancelReason;
  private String paymentType;
  private LocalDate nextPaymentDate;
  private Integer subscriptionRound;
  private String subscriptionStatus;
  private Boolean isSubscriptionTerminated;
  private String billingFailureReason;

  public void setSubscriptionStatus(String subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }

  public void setSubscriptionTerminated(Boolean subscriptionTerminated) {
    this.isSubscriptionTerminated = subscriptionTerminated;
  }

  public void setBillingFailureReason(String billingFailureReason) {
    this.billingFailureReason = billingFailureReason;
  }
}
