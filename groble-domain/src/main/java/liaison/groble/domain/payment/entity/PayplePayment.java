package liaison.groble.domain.payment.entity;

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
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payple_payments",
    indexes = {
      @Index(name = "idx_payple_payment_order_id", columnList = "order_id"),
      @Index(name = "idx_payple_payment_user_id", columnList = "user_id"),
      @Index(name = "idx_payple_payment_billing_key", columnList = "billing_key"),
      @Index(name = "idx_payple_payment_status", columnList = "status"),
      @Index(name = "idx_payple_payment_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayplePayment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false, unique = true)
  private String orderId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "pay_method", nullable = false)
  private String payMethod; // transfer, card

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PayplePaymentStatus status = PayplePaymentStatus.PENDING;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "billing_key")
  private String billingKey; // 빌링키 (정기결제용)

  @Column(name = "payer_id")
  private String payerId; // Payple 결제자 고유 ID

  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  @Column(name = "card_name")
  private String cardName;

  @Column(name = "card_number")
  private String cardNumber;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "bank_account")
  private String bankAccount;

  @Column(name = "fail_reason")
  private String failReason;

  @Column(name = "cancel_reason")
  private String cancelReason;

  @Column(name = "canceled_at")
  private LocalDateTime canceledAt;

  @Column(name = "receipt_url")
  private String receiptUrl;

  @Builder
  public PayplePayment(
      String orderId,
      Long userId,
      BigDecimal amount,
      String payMethod,
      PayplePaymentStatus status,
      String productName,
      String billingKey) {
    this.orderId = orderId;
    this.userId = userId;
    this.amount = amount;
    this.payMethod = payMethod;
    this.status = status;
    this.productName = productName;
    this.billingKey = billingKey;
  }

  // 비즈니스 메서드
  public void complete(String payerId, String paymentTime, String cardName, String cardNumber) {
    this.status = PayplePaymentStatus.COMPLETED;
    this.payerId = payerId;
    this.paymentDate = LocalDateTime.now();
    this.cardName = cardName;
    this.cardNumber = cardNumber;
  }

  public void fail(String reason) {
    this.status = PayplePaymentStatus.FAILED;
    this.failReason = reason;
  }

  public void cancel(String reason) {
    this.status = PayplePaymentStatus.CANCELLED;
    this.cancelReason = reason;
    this.canceledAt = LocalDateTime.now();
  }

  public void invalidateBillingKey() {
    this.billingKey = null;
  }

  public void updateAuthInfo(String authKey, String payReqKey, String payerId) {
    if (payerId != null && !payerId.isEmpty()) {
      this.payerId = payerId;
    }
    // 필요한 경우 authKey와 payReqKey를 저장할 필드를 추가할 수 있습니다
  }

  public void setReceiptUrl(String receiptUrl) {
    this.receiptUrl = receiptUrl;
  }
}
