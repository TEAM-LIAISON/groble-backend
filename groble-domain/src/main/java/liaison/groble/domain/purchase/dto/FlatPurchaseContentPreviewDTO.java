package liaison.groble.domain.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatPurchaseContentPreviewDTO {
  private String merchantUid;
  private Long contentId;
  private String contentType;
  private LocalDateTime purchasedAt;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private BigDecimal originalPrice;
  private BigDecimal finalPrice;
  private int priceOptionLength;
  private String orderStatus;
  private String paymentType;
  private Integer subscriptionRound;
  private String subscriptionStatus;
  private Boolean isSubscriptionTerminated;
  private String billingFailureReason;
}
