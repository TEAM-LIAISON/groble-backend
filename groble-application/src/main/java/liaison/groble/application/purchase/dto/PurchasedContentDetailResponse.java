package liaison.groble.application.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchasedContentDetailResponse {
  // 주문 기본 정보
  private final String merchantUid;

  // 구매 시간
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private final LocalDateTime purchasedAt;

  // 콘텐츠 ID
  private final Long contentId;

  // 콘텐츠 제목
  private final String contentTitle;

  // 메이커 이름
  private final String sellerName;

  // 무료 상품 여부
  private final Boolean isFreePurchase;

  // 주문금액 (원가)
  private final BigDecimal originalPrice;

  // 할인금액 (쿠폰 등)
  private final BigDecimal discountPrice;

  // 총 결제 금액
  private final BigDecimal finalPrice;
}
