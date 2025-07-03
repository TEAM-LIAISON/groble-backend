package liaison.groble.application.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchasedContentDetailDTO {
  // 구매 상태 [결제완료(PAID)/기간만료(EXPIRED)/결제취소(FAILED)]
  private final String orderStatus;

  // 주문 기본 정보
  private final String merchantUid;

  // 구매 시간
  private final LocalDateTime purchasedAt;

  // 콘텐츠 ID
  private final Long contentId;

  // 메이커 이름
  private final String sellerName;

  // 콘텐츠 제목
  private final String contentTitle;

  // 선택된 옵션 이름
  private final String selectedOptionName;

  // 선택된 옵션 개수
  private final Integer selectedOptionQuantity;

  // 선택된 옵션의 유형
  private final String selectedOptionType;

  // 파일 객체인 경우 다운로드 URL / 링크 문자열인 경우 링크 URL
  private final String documentOptionActionUrl;

  // 무료 상품 여부
  private final Boolean isFreePurchase;

  // 주문금액
  private final BigDecimal originalPrice;

  // 할인금액
  private final BigDecimal discountPrice;

  // 총 결제 금액
  private final BigDecimal finalPrice;

  // 결제 수단 상세 정보
  private String payType;
  private String payCardName;
  private String payCardNum;
}
