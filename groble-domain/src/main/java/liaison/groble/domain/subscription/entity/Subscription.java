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

  @Column(name = "last_billing_attempt_at")
  private LocalDateTime lastBillingAttemptAt;

  @Column(name = "last_billing_succeeded_at")
  private LocalDateTime lastBillingSucceededAt;

  @Column(name = "billing_retry_count", nullable = false)
  private int billingRetryCount;

  @Column(name = "grace_period_ends_at")
  private LocalDateTime gracePeriodEndsAt;

  @Column(name = "last_billing_failure_reason", length = 255)
  private String lastBillingFailureReason;

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

    Subscription subscription =
        Subscription.builder()
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

    subscription.initializeBillingState(LocalDateTime.now());
    return subscription;
  }

  public void markCancelled(LocalDateTime cancelledAt) {
    this.status = SubscriptionStatus.CANCELLED;
    this.cancelledAt = cancelledAt != null ? cancelledAt : LocalDateTime.now();
    this.billingRetryCount = 0;
    this.gracePeriodEndsAt = null;
  }

  public void renew(
      Purchase purchase,
      Payment payment,
      Long optionId,
      String optionName,
      BigDecimal price,
      String billingKey,
      LocalDate nextBillingDate) {
    if (purchase == null || payment == null) {
      throw new IllegalArgumentException("Renewal requires valid purchase and payment.");
    }
    if (billingKey == null || billingKey.isBlank()) {
      throw new IllegalArgumentException("Billing key is required for subscription renewal.");
    }

    this.purchase = purchase;
    this.payment = payment;
    this.optionId = optionId;
    this.optionName = optionName;
    this.price = price;
    this.billingKey = billingKey;
    this.nextBillingDate = nextBillingDate;
    this.status = SubscriptionStatus.ACTIVE;
    this.cancelledAt = null;
    markBillingSuccess(LocalDateTime.now());
  }

  public void resume(String billingKey, LocalDate nextBillingDate) {
    if (billingKey == null || billingKey.isBlank()) {
      throw new IllegalArgumentException("Billing key is required to resume subscription.");
    }
    if (nextBillingDate == null) {
      throw new IllegalArgumentException("Next billing date is required to resume subscription.");
    }

    this.billingKey = billingKey;
    this.nextBillingDate = nextBillingDate;
    this.status = SubscriptionStatus.ACTIVE;
    this.cancelledAt = null;
    this.billingRetryCount = 0;
    this.lastBillingAttemptAt = null;
    clearGracePeriod();
  }

  private void initializeBillingState(LocalDateTime now) {
    this.lastBillingAttemptAt = now;
    this.lastBillingSucceededAt = now;
    this.billingRetryCount = 0;
  }

  public void recordBillingAttempt(LocalDateTime attemptAt) {
    this.lastBillingAttemptAt = attemptAt;
  }

  public void markBillingSuccess(LocalDateTime successAt) {
    this.lastBillingSucceededAt = successAt;
    this.lastBillingAttemptAt = successAt;
    this.billingRetryCount = 0;
    this.lastBillingFailureReason = null;
    this.status = SubscriptionStatus.ACTIVE;
    clearGracePeriod();
  }

  public void markBillingFailure(LocalDateTime attemptAt) {
    this.lastBillingAttemptAt = attemptAt;
    this.billingRetryCount = this.billingRetryCount + 1;
    if (this.status != SubscriptionStatus.CANCELLED) {
      this.status = SubscriptionStatus.PAST_DUE;
    }
  }

  public void markBillingFailure(LocalDateTime attemptAt, String failureReason) {
    this.lastBillingAttemptAt = attemptAt;
    this.billingRetryCount = this.billingRetryCount + 1;
    this.lastBillingFailureReason = failureReason;
    if (this.status != SubscriptionStatus.CANCELLED) {
      this.status = SubscriptionStatus.PAST_DUE;
    }
  }

  public boolean canAttemptBilling(LocalDate today, LocalDateTime now, long retryIntervalMinutes) {
    if (today == null || now == null) {
      return false;
    }
    if (this.status == SubscriptionStatus.CANCELLED) {
      return false;
    }
    if (this.nextBillingDate == null || this.nextBillingDate.isAfter(today)) {
      return false;
    }

    if (retryIntervalMinutes <= 0 || this.lastBillingAttemptAt == null) {
      return true;
    }

    return java.time.Duration.between(this.lastBillingAttemptAt, now).toMinutes()
        >= retryIntervalMinutes;
  }

  public void startGracePeriod(LocalDateTime startAt, int gracePeriodDays) {
    if (startAt == null || gracePeriodDays <= 0) {
      this.gracePeriodEndsAt = null;
      return;
    }

    this.gracePeriodEndsAt = startAt.plusDays(gracePeriodDays);
  }

  public boolean isGracePeriodActive(LocalDateTime now) {
    if (now == null || gracePeriodEndsAt == null) {
      return false;
    }
    return !now.isAfter(gracePeriodEndsAt);
  }

  public void clearGracePeriod() {
    this.gracePeriodEndsAt = null;
  }
}
