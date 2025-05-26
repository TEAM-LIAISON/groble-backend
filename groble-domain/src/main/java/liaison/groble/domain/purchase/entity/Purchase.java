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

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.purchase.enums.PurchaseStatus;
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
      @Index(name = "idx_purchase_status", columnList = "status"),
      @Index(name = "idx_purchase_user_content", columnList = "user_id, content_id", unique = true),
      @Index(name = "idx_purchase_created_at", columnList = "created_at")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Purchase extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "payment_id")
  private Payment payment;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PurchaseStatus status = PurchaseStatus.COMPLETED;

  // 구매 시점의 금액 정보 (기록용)
  @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "discount_amount", precision = 10, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal finalPrice;

  // 쿠폰 정보 (구매 시점 기록용)
  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "used_coupon_id")
  private UserCoupon usedCoupon;

  @Column(name = "coupon_code")
  private String couponCode;

  @Column(name = "coupon_discount_amount", precision = 10, scale = 2)
  private BigDecimal couponDiscountAmount = BigDecimal.ZERO;

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

  // 구매 확정 관련
  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @Column(name = "refund_requested_at")
  private LocalDateTime refundRequestedAt;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  @Column(name = "refund_reason")
  private String refundReason;

  // 비즈니스 메서드들
  public void complete() {
    if (this.status != PurchaseStatus.PENDING) {
      throw new IllegalStateException("대기 상태에서만 구매 완료 처리가 가능합니다.");
    }

    this.status = PurchaseStatus.COMPLETED;
    this.purchasedAt = LocalDateTime.now();
  }

  public void cancel(String reason) {
    if (this.status != PurchaseStatus.COMPLETED) {
      throw new IllegalStateException("완료 상태에서만 구매 취소가 가능합니다.");
    }

    this.status = PurchaseStatus.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
    this.cancelReason = reason;
  }

  public void confirm() {
    if (this.status != PurchaseStatus.COMPLETED) {
      throw new IllegalStateException("완료 상태에서만 구매 확정이 가능합니다.");
    }

    this.status = PurchaseStatus.CONFIRMED;
    this.confirmedAt = LocalDateTime.now();
  }

  public void requestRefund(String reason) {
    if (this.status != PurchaseStatus.COMPLETED && this.status != PurchaseStatus.CONFIRMED) {
      throw new IllegalStateException("완료 또는 확정 상태에서만 환불 요청이 가능합니다.");
    }

    this.status = PurchaseStatus.REFUND_REQUESTED;
    this.refundRequestedAt = LocalDateTime.now();
    this.refundReason = reason;
  }

  public void processRefund() {
    if (this.status != PurchaseStatus.REFUND_REQUESTED) {
      throw new IllegalStateException("환불 요청 상태에서만 환불 처리가 가능합니다.");
    }

    this.status = PurchaseStatus.REFUNDED;
    this.refundedAt = LocalDateTime.now();
  }

  public void fail(String reason) {
    this.status = PurchaseStatus.FAILED;
    this.cancelReason = reason;
  }

  // 팩토리 메서드
  public static Purchase createFromOrder(Order order) {
    if (order.getOrderItems().isEmpty()) {
      throw new IllegalArgumentException("주문 아이템이 없습니다.");
    }

    // 첫 번째 주문 아이템을 기준으로 구매 생성 (단일 콘텐츠 구매 가정)
    var orderItem = order.getOrderItems().get(0);

    return Purchase.builder()
        .user(order.getUser())
        .content(orderItem.getContent())
        .order(order)
        .payment(order.getPayment())
        .status(PurchaseStatus.PENDING)
        .originalPrice(order.getOriginalAmount())
        .discountAmount(order.getDiscountAmount())
        .finalPrice(order.getFinalAmount())
        .usedCoupon(order.getAppliedCoupon())
        .couponCode(
            order.getAppliedCoupon() != null ? order.getAppliedCoupon().getCouponCode() : null)
        .couponDiscountAmount(order.getCouponDiscountAmount())
        .selectedOptionType(
            orderItem.getOptionType() != null
                ? OptionType.valueOf(orderItem.getOptionType().name())
                : null)
        .selectedOptionId(orderItem.getOptionId())
        //                .selectedOptionName(orderItem.getOptionName())
        .build();
  }

  // 편의 메서드들
  public boolean isCompleted() {
    return status == PurchaseStatus.COMPLETED;
  }

  public boolean isCancelled() {
    return status == PurchaseStatus.CANCELLED;
  }

  public boolean isRefunded() {
    return status == PurchaseStatus.REFUNDED;
  }

  public boolean hasDiscount() {
    return discountAmount.compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean hasCouponApplied() {
    return usedCoupon != null;
  }

  // 내부 열거형
  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
