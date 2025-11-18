package liaison.groble.application.content.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentPayPageDTO {
  // 사용자가 로그인했는지 여부
  private final Boolean isLoggedIn;
  // 콘텐츠 썸네일 URL
  private final String thumbnailUrl;
  // 판매자 이름
  private final String sellerName;
  // 콘텐츠 이름
  private final String title;
  // 콘텐츠 유형
  private final String contentType;
  // 결제 유형
  private final String paymentType;
  // 다음 결제 예정일 (정기 결제 시)
  private final LocalDate nextPaymentDate;
  // 정기결제 관련 정보
  private final SubscriptionMetaDTO subscriptionMeta;
  // 옵션 이름
  private final String optionName;
  // 옵션에 속하는 가격
  private final BigDecimal price;
  // 사용자가 보유한 쿠폰 목록
  private final List<UserCouponDTO> userCoupons;

  @Getter
  @Builder
  public static class UserCouponDTO {
    private final String couponCode;
    private final String name;
    private final String couponType;
    private final BigDecimal discountValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime validUntil;

    private final BigDecimal minOrderPrice;
  }

  @Getter
  @Builder
  public static class SubscriptionMetaDTO {
    private final boolean hasActiveBillingKey;
    private final String billingKeyId;
    private final String merchantUserKey;
    private final String defaultPayMethod;
    private final String payWork;
    private final String cardVer;
    private final String regularFlag;
    private final LocalDate nextPaymentDate;
    private final String payYear;
    private final String payMonth;
    private final String payDay;
    private final boolean requiresImmediateCharge;
  }
}
