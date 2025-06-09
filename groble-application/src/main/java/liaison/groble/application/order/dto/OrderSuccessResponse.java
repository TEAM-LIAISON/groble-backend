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

  // 구매 시간
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private final LocalDateTime purchasedAt;

  // 콘텐츠 ID
  private final Long contentId;

  // 콘텐츠 제목
  private final String contentTitle;

  // 구매 상태 [결제완료(PAID)/기간만료(EXPIRED)/결제취소(FAILED)]
  private final String orderStatus;

  // 상품 정보
  private final String contentDescription;
  private final String contentThumbnailUrl;

  // 선택된 옵션 정보
  private final Long selectedOptionId;
  private final String selectedOptionType;

  // 가격 정보
  private final BigDecimal originalPrice;
  private final BigDecimal discountPrice;
  private final BigDecimal finalPrice;

  private final Boolean isFreePurchase;
}
