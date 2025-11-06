package liaison.groble.domain.content.dto;

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
public class FlatContentPreviewDTO {
  private Long contentId;
  private LocalDateTime createdAt;
  private String title;
  private String thumbnailUrl;
  private String sellerName;
  private String sellerProfileImageUrl;
  private BigDecimal lowestPrice;
  private int priceOptionLength;
  private Boolean isAvailableForSale;
  private String status;
  private Boolean isDeletable;
  private Boolean isSearchExposed;
  private String categoryId;
  private String contentType;
  private String paymentType;
  private String subscriptionSellStatus;
}
