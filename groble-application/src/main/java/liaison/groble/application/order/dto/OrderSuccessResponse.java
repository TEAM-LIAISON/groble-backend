package liaison.groble.application.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSuccessResponse {
  // 주문 기본 정보
  private final String merchantUid;
  private final String orderNumber;
  private final String orderStatus;
  private final String purchaseStatus;

  // 상품 정보
  private final Long contentId;
  private final String contentTitle;
  private final String contentDescription;
  private final String contentThumbnailUrl;

  // 선택된 옵션 정보
  private final Long selectedOptionId;
  private final String selectedOptionType;

  // 가격 정보
  private final BigDecimal originalPrice;
  private final BigDecimal discountPrice;
  private final BigDecimal finalPrice;

  // 구매 정보
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private final LocalDateTime purchasedAt;

  private final Boolean isFreePurchase;
}
