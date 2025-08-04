package liaison.groble.domain.coupon.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.coupon.enums.CouponType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(
    name = "coupon_templates",
    indexes = {
      @Index(name = "idx_coupon_template_name", columnList = "name"),
      @Index(name = "idx_coupon_template_active", columnList = "is_active"),
      @Index(name = "idx_coupon_template_validity", columnList = "valid_from, valid_until"),
      @Index(name = "idx_coupon_template_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponTemplate extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "coupon_type", nullable = false)
  private CouponType couponType;

  @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountValue; // 퍼센트 또는 고정금액

  @Column(name = "min_order_price", precision = 10, scale = 2)
  private BigDecimal minOrderPrice; // 최소 주문금액

  @Column(name = "max_discount_price", precision = 10, scale = 2)
  private BigDecimal maxDiscountPrice; // 최대 할인금액 (퍼센트 쿠폰용)

  @Column(name = "usage_limit_per_user")
  private Integer usageLimitPerUser = 1; // 사용자당 사용횟수 제한

  @Column(name = "total_usage_limit")
  private Integer totalUsageLimit; // 전체 사용횟수 제한

  @Column(name = "current_usage_count")
  private Integer currentUsageCount = 0; // 현재 사용횟수

  @Column(name = "valid_from", nullable = false)
  private LocalDateTime validFrom;

  @Column(name = "valid_until", nullable = false)
  private LocalDateTime validUntil;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @OneToMany(mappedBy = "couponTemplate", cascade = CascadeType.ALL)
  private List<UserCoupon> userCoupons = new ArrayList<>();

  @Builder
  public CouponTemplate(
      String name,
      String description,
      CouponType couponType,
      BigDecimal discountValue,
      BigDecimal minOrderPrice,
      BigDecimal maxDiscountPrice,
      Integer usageLimitPerUser,
      Integer totalUsageLimit,
      LocalDateTime validFrom,
      LocalDateTime validUntil) {
    this.name = name;
    this.description = description;
    this.couponType = couponType;
    this.discountValue = discountValue;
    this.minOrderPrice = minOrderPrice;
    this.maxDiscountPrice = maxDiscountPrice;
    this.usageLimitPerUser = usageLimitPerUser;
    this.totalUsageLimit = totalUsageLimit;
    this.validFrom = validFrom;
    this.validUntil = validUntil;
  }

  // 비즈니스 메서드
  public boolean isUsable() {
    LocalDateTime now = LocalDateTime.now();

    boolean result =
        isActive
            && validFrom.isBefore(now)
            && validUntil.isAfter(now)
            && (totalUsageLimit == null || currentUsageCount < totalUsageLimit);

    log.debug(
        "▶ CouponTemplate.isUsable() - result: {}, isActive: {}, now: {}, validFrom: {}, validUntil: {}, currentUsageCount: {}, totalUsageLimit: {}",
        result,
        isActive,
        now,
        validFrom,
        validUntil,
        currentUsageCount,
        totalUsageLimit);

    return result;
  }

  public void incrementUsageCount() {
    this.currentUsageCount++;
  }

  public void decrementUsageCount() {
    if (this.currentUsageCount > 0) {
      this.currentUsageCount--;
    }
  }

  public BigDecimal calculateDiscountPrice(BigDecimal orderPrice) {
    if (orderPrice == null || orderPrice.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // 최소 주문금액 체크
    if (minOrderPrice != null && orderPrice.compareTo(minOrderPrice) < 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal discountPrice = BigDecimal.ZERO;

    switch (couponType) {
      case PERCENTAGE:
        discountPrice = orderPrice.multiply(discountValue).divide(BigDecimal.valueOf(100));

        // 최대 할인금액 제한
        if (maxDiscountPrice != null && discountPrice.compareTo(maxDiscountPrice) > 0) {
          discountPrice = maxDiscountPrice;
        }
        break;

      case FIXED_PRICE:
        discountPrice = discountValue;
        // 주문금액보다 클 수 없음
        if (discountPrice.compareTo(orderPrice) > 0) {
          discountPrice = orderPrice;
        }
        break;
    }

    return discountPrice;
  }
}
