package liaison.groble.application.order.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 최종 주문 확정 응답 DTO
 *
 * <p>이 DTO는 쿠폰이 적용된 최종 주문 정보를 담아 컨트롤러로 전달합니다. 클라이언트는 이 정보를 기반으로 실제 결제를 진행합니다.
 */
@Getter
@Builder
public class FinalizeOrderResponse {

  // 주문 기본 정보
  private final Long orderId;
  private final String merchantUid;

  // 금액 정보 - 쿠폰 적용 전후를 모두 제공하여 투명성 확보
  private final BigDecimal originalPrice; // 원래 금액 (할인 전)
  private final BigDecimal couponDiscountPrice; // 쿠폰 할인 금액
  private final BigDecimal finalPrice; // 최종 결제 금액

  // 적용된 쿠폰 정보
  private final List<AppliedCouponInfo> appliedCoupons;

  // 주문 항목 상세
  private final List<OrderItemInfo> orderItems;

  // 결제 준비 상태
  private final boolean readyForPayment;
  private final String paymentMessage;

  /** 적용된 쿠폰 정보 */
  @Getter
  @Builder
  public static class AppliedCouponInfo {
    private final String couponCode;
    private final String couponName;
    private final BigDecimal discountPrice;
    private final String discountType; // PERCENTAGE, FIXED_PRICE
  }

  /** 주문 항목 정보 */
  @Getter
  @Builder
  public static class OrderItemInfo {
    private final Long optionId;
    private final String optionType;
    private final String optionName;
    private final Integer quantity;
    private final BigDecimal price;
    private final BigDecimal totalPrice;
  }
}
