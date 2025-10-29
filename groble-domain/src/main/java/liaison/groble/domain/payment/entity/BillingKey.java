package liaison.groble.domain.payment.entity;

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
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.payment.enums.BillingKeyStatus;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "billing_keys",
    indexes = {
      @Index(name = "idx_billing_keys_user", columnList = "user_id"),
      @Index(name = "idx_billing_keys_status", columnList = "status")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillingKey extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "billing_key", nullable = false, length = 255, unique = true)
  private String billingKey;

  @Column(name = "card_name", length = 64)
  private String cardName;

  @Column(name = "card_number_masked", length = 64)
  private String cardNumberMasked;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BillingKeyStatus status;

  @Column(name = "last_used_at")
  private LocalDateTime lastUsedAt;

  @Builder(access = AccessLevel.PRIVATE)
  private BillingKey(
      User user,
      String billingKey,
      String cardName,
      String cardNumberMasked,
      BillingKeyStatus status) {
    this.user = user;
    this.billingKey = billingKey;
    this.cardName = cardName;
    this.cardNumberMasked = cardNumberMasked;
    this.status = status;
  }

  public static BillingKey active(
      User user, String billingKey, String cardName, String cardNumberMasked) {
    if (user == null) {
      throw new IllegalArgumentException("빌링키 등록은 회원만 가능합니다.");
    }
    if (billingKey == null || billingKey.isBlank()) {
      throw new IllegalArgumentException("빌링키 값이 비어 있습니다.");
    }

    return BillingKey.builder()
        .user(user)
        .billingKey(billingKey)
        .cardName(cardName)
        .cardNumberMasked(cardNumberMasked)
        .status(BillingKeyStatus.ACTIVE)
        .build();
  }

  public void deactivate() {
    this.status = BillingKeyStatus.INACTIVE;
  }

  public boolean isActive() {
    return this.status == BillingKeyStatus.ACTIVE;
  }

  public void markUsed() {
    this.lastUsedAt = LocalDateTime.now();
  }
}
