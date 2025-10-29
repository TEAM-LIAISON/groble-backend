package liaison.groble.domain.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "subscriptions",
    indexes = {
      @Index(name = "idx_subscription_user", columnList = "user_id"),
      @Index(name = "idx_subscription_content", columnList = "content_id"),
      @Index(name = "idx_subscription_status", columnList = "status")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_id", nullable = false, unique = true)
  private Purchase purchase;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false, unique = true)
  private Payment payment;

  @Column(name = "option_id", nullable = false)
  private Long optionId;

  @Column(name = "option_name", length = 60)
  private String optionName;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "billing_key", nullable = false, length = 255)
  private String billingKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SubscriptionStatus status;

  @Column(name = "next_billing_date")
  private LocalDate nextBillingDate;

  @Column(name = "activated_at", nullable = false)
  private LocalDateTime activatedAt;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  @Builder(access = AccessLevel.PRIVATE)
  private Subscription(
      User user,
      Content content,
      Purchase purchase,
      Payment payment,
      Long optionId,
      String optionName,
      BigDecimal price,
      String billingKey,
      SubscriptionStatus status,
      LocalDate nextBillingDate,
      LocalDateTime activatedAt) {
    this.user = user;
    this.content = content;
    this.purchase = purchase;
    this.payment = payment;
    this.optionId = optionId;
    this.optionName = optionName;
    this.price = price;
    this.billingKey = billingKey;
    this.status = status;
    this.nextBillingDate = nextBillingDate;
    this.activatedAt = activatedAt;
  }

  public static Subscription create(
      User user,
      Content content,
      Purchase purchase,
      Payment payment,
      Long optionId,
      String optionName,
      BigDecimal price,
      String billingKey,
      LocalDate nextBillingDate) {

    if (user == null) {
      throw new IllegalArgumentException("Subscription requires a member user.");
    }
    if (billingKey == null || billingKey.isBlank()) {
      throw new IllegalArgumentException("Billing key is required for subscription.");
    }

    return Subscription.builder()
        .user(user)
        .content(content)
        .purchase(purchase)
        .payment(payment)
        .optionId(optionId)
        .optionName(optionName)
        .price(price)
        .billingKey(billingKey)
        .status(SubscriptionStatus.ACTIVE)
        .nextBillingDate(nextBillingDate)
        .activatedAt(LocalDateTime.now())
        .build();
  }

  public void markCancelled(LocalDateTime cancelledAt) {
    this.status = SubscriptionStatus.CANCELLED;
    this.cancelledAt = cancelledAt != null ? cancelledAt : LocalDateTime.now();
  }
}
