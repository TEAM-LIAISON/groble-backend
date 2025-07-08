package liaison.groble.domain.purchase.entity;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "purchases",
    indexes = {
      @Index(name = "idx_purchase_user", columnList = "user_id"),
      @Index(name = "idx_purchase_content", columnList = "content_id"),
      @Index(name = "idx_purchase_order", columnList = "order_id", unique = true),
      @Index(name = "idx_purchase_created_at", columnList = "created_at")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Purchase extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 구매자 정보 (누가 상품을 구매했는지)
  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 구매한 콘텐츠 정보 (어떤 콘텐츠를 구매했는지)
  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  // 어떤 주문을 통해 구매했는지 (1:1 관계)
  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  // 결제 정보 (1:1 관계)
  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "payment_id")
  private Payment payment;

  // 구매 시점의 금액 정보 (기록용)
  @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "discount_price", precision = 10, scale = 2)
  private BigDecimal discountPrice = BigDecimal.ZERO;

  @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal finalPrice;

  // 쿠폰 정보 (구매 시점 기록용)
  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "used_coupon_id")
  private UserCoupon usedCoupon;

  @Column(name = "coupon_code")
  private String couponCode;

  @Column(name = "coupon_discount_price", precision = 10, scale = 2)
  private BigDecimal couponDiscountPrice = BigDecimal.ZERO;

  // 선택된 옵션 정보
  @Column(name = "selected_option_type")
  @Enumerated(EnumType.STRING)
  private OptionType selectedOptionType;

  @Column(name = "selected_option_id")
  private Long selectedOptionId;

  @Column(name = "selected_option_name")
  private String selectedOptionName;

  // 구매 완료/취소 시간
  @Column(name = "purchased_at")
  private LocalDateTime purchasedAt;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  @Column(name = "cancel_reason")
  private String cancelReason;

  @Column(name = "refund_requested_at")
  private LocalDateTime refundRequestedAt;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  @Column(name = "refund_reason")
  private String refundReason;

  // 팩토리 메서드
  public static Purchase createFromOrder(Order order) {
    if (order.getOrderItems().isEmpty()) {
      throw new IllegalArgumentException("주문 아이템이 없습니다.");
    }

    var orderItem = order.getOrderItems().get(0);

    // 선택된 옵션의 이름 찾기
    String selectedOptionName =
        orderItem.getContent().getOptions().stream()
            .filter(option -> option.getId().equals(orderItem.getOptionId()))
            .findFirst()
            .map(ContentOption::getName)
            .orElse(null);

    return Purchase.builder()
        .user(order.getUser())
        .content(orderItem.getContent())
        .order(order)
        .payment(order.getPayment())
        .originalPrice(order.getOriginalPrice())
        .discountPrice(order.getDiscountPrice())
        .finalPrice(order.getFinalPrice())
        .usedCoupon(order.getAppliedCoupon())
        .couponCode(
            order.getAppliedCoupon() != null ? order.getAppliedCoupon().getCouponCode() : null)
        .couponDiscountPrice(order.getCouponDiscountPrice())
        .selectedOptionType(
            orderItem.getOptionType() != null
                ? OptionType.valueOf(orderItem.getOptionType().name())
                : null)
        .selectedOptionName(selectedOptionName)
        .purchasedAt(LocalDateTime.now())
        .selectedOptionId(orderItem.getOptionId())
        .build();
  }

  // 내부 열거형
  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
