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
public class FlatPurchaseContentDetailDTO {
  private String orderStatus;
  private String merchantUid;
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
}
