package liaison.groble.application.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseContentCardDto {
  private String merchantUid;
  private Long contentId;
  private LocalDateTime purchasedAt;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private BigDecimal originalPrice;
  private int priceOptionLength;
  private String status;
}
