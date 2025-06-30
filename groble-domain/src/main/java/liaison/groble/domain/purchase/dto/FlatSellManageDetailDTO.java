package liaison.groble.domain.purchase.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatSellManageDetailDTO {
  private BigDecimal totalPaymentPrice;
  private Long totalPurchaseCustomer;
  private Long totalReviewCount;
}
