package liaison.groble.domain.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.coupon.entity.UserCoupon;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.purchase.entity.Purchaser;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "orders",
    indexes = {
      @Index(name = "idx_order_user", columnList = "user_id"),
      @Index(name = "idx_order_merchant_uid", columnList = "merchant_uid", unique = true),
      @Index(name = "idx_order_status", columnList = "status"),
      @Index(name = "idx_order_created_at", columnList = "created_at"),
      @Index(name = "idx_order_coupon", columnList = "applied_coupon_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "merchant_uid", nullable = false, unique = true)
  private String merchantUid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.PENDING;

  // 가격 관련 필드들
  @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalAmount; // 원래 금액 (할인 적용 전)

  @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO; // 할인 금액

  @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal finalAmount; // 최종 결제 금액

  // 쿠폰 관련 필드들
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "applied_coupon_id")
  private UserCoupon appliedCoupon; // 적용된 쿠폰

  @Column(name = "coupon_discount_amount", precision = 10, scale = 2)
  private BigDecimal couponDiscountAmount = BigDecimal.ZERO; // 쿠폰 할인 금액

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Payment payment;

  @Embedded private Purchaser purchaser;

  @Column(name = "order_note", columnDefinition = "TEXT")
  private String orderNote;

  @Version private Long version;

  // 생성자 수정
  @Builder(access = AccessLevel.PACKAGE)
  private Order(
      User user,
      BigDecimal originalAmount,
      UserCoupon appliedCoupon,
      Purchaser purchaser,
      String orderNote) {
    this.user = user;
    this.originalAmount = originalAmount;
    this.appliedCoupon = appliedCoupon;
    this.purchaser = purchaser;
    this.orderNote = orderNote;
    this.merchantUid = generateMerchantUid();

    // 쿠폰 할인 금액 계산
    if (appliedCoupon != null) {
      this.couponDiscountAmount =
          appliedCoupon.getCouponTemplate().calculateDiscountAmount(originalAmount);
    }

    // 총 할인 금액 및 최종 금액 계산
    this.discountAmount = this.couponDiscountAmount;
    this.finalAmount = this.originalAmount.subtract(this.discountAmount);
  }

  // 비즈니스 메서드
  public void completePayment() {
    validateStateTransition(OrderStatus.PAID);
    this.status = OrderStatus.PAID;

    // 쿠폰 사용 처리
    if (appliedCoupon != null) {
      appliedCoupon.use(this);
    }
  }

  public void cancelOrder(String reason) {
    validateStateTransition(OrderStatus.CANCELLED);
    this.status = OrderStatus.CANCELLED;
    this.orderNote = reason;

    // 쿠폰 사용 취소 처리
    if (appliedCoupon != null) {
      appliedCoupon.cancel();
    }
  }

  public void failOrder(String reason) {
    validateStateTransition(OrderStatus.FAILED);
    this.status = OrderStatus.FAILED;
    this.orderNote = reason;

    // 쿠폰 사용 취소 처리
    if (appliedCoupon != null) {
      appliedCoupon.cancel();
    }
  }

  // 쿠폰 적용 메서드
  public void applyCoupon(UserCoupon coupon) {
    if (coupon == null) {
      return;
    }

    if (!coupon.isUsable()) {
      throw new IllegalArgumentException("사용할 수 없는 쿠폰입니다.");
    }

    // 기존 쿠폰이 있다면 제거
    if (this.appliedCoupon != null) {
      removeCoupon();
    }

    this.appliedCoupon = coupon;
    this.couponDiscountAmount =
        coupon.getCouponTemplate().calculateDiscountAmount(this.originalAmount);

    // 할인 금액 및 최종 금액 재계산
    recalculateAmounts();
  }

  // 쿠폰 제거 메서드
  public void removeCoupon() {
    this.appliedCoupon = null;
    this.couponDiscountAmount = BigDecimal.ZERO;
    recalculateAmounts();
  }

  // 금액 재계산 메서드
  private void recalculateAmounts() {
    this.discountAmount = this.couponDiscountAmount;
    this.finalAmount = this.originalAmount.subtract(this.discountAmount);

    // 최종 금액이 0보다 작을 수 없음
    if (this.finalAmount.compareTo(BigDecimal.ZERO) < 0) {
      this.finalAmount = BigDecimal.ZERO;
      this.discountAmount = this.originalAmount;
    }
  }

  private void validateStateTransition(OrderStatus newStatus) {
    if (this.status == newStatus) {
      return;
    }

    switch (this.status) {
      case PENDING:
        if (newStatus != OrderStatus.PAID
            && newStatus != OrderStatus.CANCELLED
            && newStatus != OrderStatus.FAILED) {
          throw new IllegalStateException("대기 상태에서는 결제완료, 취소, 실패 상태로만 변경 가능합니다.");
        }
        break;
      case PAID:
        if (newStatus != OrderStatus.CANCELLED) {
          throw new IllegalStateException("결제완료 상태에서는 취소 상태로만 변경 가능합니다.");
        }
        break;
      case CANCELLED:
      case FAILED:
        throw new IllegalStateException("이미 종료된 주문은 상태를 변경할 수 없습니다.");
    }
  }

  // 연관관계 편의 메서드
  public void addOrderItem(
      Content content,
      BigDecimal price,
      OrderItem.OptionType optionType,
      Long optionId,
      String optionName,
      int quantity) {
    OrderItem orderItem =
        OrderItem.builder()
            .order(this)
            .content(content)
            .price(price)
            .quantity(quantity)
            .optionType(optionType)
            .optionId(optionId)
            .optionName(optionName)
            .build();

    this.orderItems.add(orderItem);
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  // 유틸리티 메서드
  private String generateMerchantUid() {
    return "ORD_"
        + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10)
        + "_"
        + System.currentTimeMillis();
  }

  // 팩토리 메서드 수정 (쿠폰 지원)
  public static Order createOrderWithCoupon(
      User user,
      Content content,
      OrderItem.OptionType optionType,
      Long optionId,
      String optionName,
      BigDecimal price,
      UserCoupon coupon,
      Purchaser purchaser) {

    if (content.getStatus() != ContentStatus.ACTIVE) {
      throw new IllegalArgumentException("판매중인 콘텐츠만 구매할 수 있습니다: " + content.getTitle());
    }

    Order order =
        Order.builder()
            .user(user)
            .originalAmount(price)
            .appliedCoupon(coupon)
            .purchaser(purchaser)
            .build();

    order.addOrderItem(content, price, optionType, optionId, optionName, 1);
    return order;
  }

  // 기존 팩토리 메서드 (쿠폰 없음)
  public static Order createOrderWithOption(
      User user,
      Content content,
      OrderItem.OptionType optionType,
      Long optionId,
      String optionName,
      BigDecimal price,
      Purchaser purchaser) {
    return createOrderWithCoupon(
        user, content, optionType, optionId, optionName, price, null, purchaser);
  }

  // Getter 메서드들 추가
  public BigDecimal getTotalAmount() {
    return this.finalAmount; // 기존 코드 호환성을 위해
  }

  // 할인 정보 확인 메서드들
  public boolean hasCouponApplied() {
    return appliedCoupon != null;
  }

  public boolean hasDiscount() {
    return discountAmount.compareTo(BigDecimal.ZERO) > 0;
  }

  // 내부 클래스
  public enum OrderStatus {
    PENDING("결제대기"),
    PAID("결제완료"),
    CANCELLED("결제취소"),
    EXPIRED("기간만료"),
    FAILED("결제실패");

    private final String description;

    OrderStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
