package liaison.groble.domain.coupon.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.order.entity.Order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "coupon_usage_history",
    indexes = {
      @Index(name = "idx_coupon_usage_user_coupon", columnList = "user_coupon_id"),
      @Index(name = "idx_coupon_usage_order", columnList = "order_id"),
      @Index(name = "idx_coupon_usage_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUsageHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_coupon_id", nullable = false)
  private UserCoupon userCoupon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalAmount;

  @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountAmount;

  @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal finalAmount;

  @Builder
  public CouponUsageHistory(
      UserCoupon userCoupon,
      Order order,
      BigDecimal originalAmount,
      BigDecimal discountAmount,
      BigDecimal finalAmount) {
    this.userCoupon = userCoupon;
    this.order = order;
    this.originalAmount = originalAmount;
    this.discountAmount = discountAmount;
    this.finalAmount = finalAmount;
  }
}
