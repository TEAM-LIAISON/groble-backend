package liaison.groble.application.sell.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellManageDetailDTO {
  // 총 결제 금액
  private BigDecimal totalPaymentPrice;

  // 총 구매 고객
  private Long totalPurchaseCustomer;

  // 리뷰 개수
  private Long totalReviewCount;
}
