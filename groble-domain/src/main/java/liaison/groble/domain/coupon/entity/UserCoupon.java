package liaison.groble.domain.coupon.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.coupon.enums.CouponStatus;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_coupons",
    indexes = {
      @Index(name = "idx_user_coupon_user", columnList = "user_id"),
      @Index(name = "idx_user_coupon_template", columnList = "coupon_template_id"),
      @Index(name = "idx_user_coupon_code", columnList = "coupon_code", unique = true),
      @Index(name = "idx_user_coupon_status", columnList = "status"),
      @Index(name = "idx_user_coupon_user_status", columnList = "user_id, status")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coupon_template_id", nullable = false)
  private CouponTemplate couponTemplate;

  @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
  private String couponCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CouponStatus status = CouponStatus.ISSUED;

  @Column(name = "issued_at", nullable = false)
  private LocalDateTime issuedAt;

  @Column(name = "used_at")
  private LocalDateTime usedAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order; // 사용된 주문

  @Builder
  public UserCoupon(User user, CouponTemplate couponTemplate, String couponCode) {
    this.user = user;
    this.couponTemplate = couponTemplate;
    this.couponCode = couponCode;
    this.issuedAt = LocalDateTime.now();
  }

  // 비즈니스 메서드
  public boolean isUsable() {
    return status == CouponStatus.ISSUED && couponTemplate.isUsable();
  }

  public void use(Order order) {
    if (!isUsable()) {
      throw new IllegalStateException("사용할 수 없는 쿠폰입니다.");
    }

    this.status = CouponStatus.USED;
    this.usedAt = LocalDateTime.now();
    this.order = order;

    // 쿠폰 템플릿의 사용 횟수 증가
    couponTemplate.incrementUsageCount();
  }

  public void expire() {
    if (status == CouponStatus.ISSUED) {
      this.status = CouponStatus.EXPIRED;
    }
  }

  public void cancel() {
    if (status == CouponStatus.USED) {
      this.status = CouponStatus.ISSUED;
      this.usedAt = null;
      this.order = null;

      // 쿠폰 템플릿의 사용 횟수 감소
      couponTemplate.decrementUsageCount();
    }
  }
}
