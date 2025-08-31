package liaison.groble.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.AggregateRoot;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.event.PaymentCancelledEvent;
import liaison.groble.domain.payment.event.PaymentCompletedEvent;
import liaison.groble.domain.payment.event.PaymentCreatedEvent;
import liaison.groble.domain.payment.vo.PaymentAmount;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payments",
    indexes = {
      @Index(name = "idx_payment_order", columnList = "order_id"),
      @Index(name = "idx_payment_method", columnList = "payment_method"),
      @Index(name = "idx_payment_created_at", columnList = "created_at"),
      @Index(name = "idx_payment_billing_key", columnList = "billing_key")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AggregateRoot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 주문 정보 (1:1 관계) */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  /** 결제 금액 - Value Object 사용 */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "value",
        column = @Column(name = "price", nullable = false, precision = 10, scale = 2))
  })
  private PaymentAmount amount;

  /** 결제 수단 */
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  private PaymentMethod paymentMethod;

  /** 결제 수단 상세 정보 (카드사명, 은행명 등) */
  @Column(name = "method_detail")
  private String methodDetail;

  /** PG사 고유 결제 키 (페이플: PCD_PAY_OID) */
  @Column(name = "payment_key", unique = true)
  private String paymentKey;

  /** PG사 거래 고유번호 (PCD_PAY_CARDTRADENUM) */
  @Column(name = "pg_tid")
  private String pgTid;

  /** 빌링키 (정기결제용, 페이플: PCD_PAYER_ID) */
  @Column(name = "billing_key")
  private String billingKey;

  /** 구매자 정보 */
  @Column(name = "purchaser_name")
  private String purchaserName;

  @Column(name = "purchaser_email")
  private String purchaserEmail;

  @Column(name = "purchaser_phone_number")
  private String purchaserPhoneNumber;

  /** 결제 완료 시각 */
  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  // 생성자
  @Builder
  public Payment(
      Order order,
      BigDecimal price,
      PaymentMethod paymentMethod,
      String paymentKey,
      String purchaserName,
      String purchaserEmail,
      String purchaserPhoneNumber) {
    this.order = order;
    this.amount =
        price != null && price.compareTo(BigDecimal.ZERO) > 0
            ? PaymentAmount.of(price)
            : PaymentAmount.zero();
    this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.FREE;
    this.paymentKey = paymentKey;
    this.purchaserName = purchaserName;
    this.purchaserEmail = purchaserEmail;
    this.purchaserPhoneNumber = purchaserPhoneNumber;

    // Order와의 양방향 관계 설정
    if (order != null) {
      order.setPayment(this);
    }
  }

  /**
   * 무료 결제용 팩토리 메서드
   *
   * @param order 주문 정보
   * @return 무료 결제 엔티티
   */
  public static Payment createFreePayment(Order order) {
    return Payment.builder()
        .order(order)
        .price(BigDecimal.ZERO)
        .paymentMethod(PaymentMethod.FREE)
        .purchaserName(order.getPurchaser().getName())
        .purchaserEmail(order.getPurchaser().getEmail())
        .purchaserPhoneNumber(order.getPurchaser().getPhone())
        .build();
  }

  /**
   * PG 결제용 팩토리 메서드
   *
   * @param order 주문 정보
   * @param amount 결제 금액 (Value Object)
   * @param paymentMethod 결제 수단
   * @param paymentKey PG 결제 키
   * @return PG 결제 엔티티
   */
  public static Payment createPgPayment(
      Order order, PaymentAmount amount, PaymentMethod paymentMethod, String paymentKey) {
    return Payment.builder()
        .order(order)
        .price(amount.getValue())
        .paymentMethod(paymentMethod)
        .paymentKey(paymentKey)
        .purchaserName(order.getPurchaser().getName())
        .purchaserEmail(order.getPurchaser().getEmail())
        .purchaserPhoneNumber(order.getPurchaser().getPhone())
        .build();
  }

  /**
   * 빌링 결제용 팩토리 메서드 (정기결제)
   *
   * @param order 주문 정보
   * @param amount 결제 금액 (Value Object)
   * @param paymentKey PG 결제 키
   * @return 빌링 결제 엔티티
   */
  public static Payment createBillingPayment(Order order, PaymentAmount amount, String paymentKey) {
    return Payment.builder()
        .order(order)
        .price(amount.getValue())
        .paymentMethod(PaymentMethod.BILLING)
        .paymentKey(paymentKey)
        .purchaserName(order.getPurchaser().getName())
        .purchaserEmail(order.getPurchaser().getEmail())
        .purchaserPhoneNumber(order.getPurchaser().getPhone())
        .build();
  }

  // 비즈니스 메서드

  /** 무료 결제 즉시 완료 처리 */
  public void completeFreePayment() {
    if (this.paymentMethod != PaymentMethod.FREE) {
      throw new IllegalStateException("무료 결제만 즉시 완료 처리가 가능합니다.");
    }

    this.paidAt = LocalDateTime.now();
    publishPaymentCompletedEvent();
  }

  /**
   * PG 결제 완료 처리
   *
   * @param pgTid PG 거래 ID
   * @param methodDetail 결제 수단 상세
   */
  public void completePgPayment(String pgTid, String methodDetail) {
    if (this.paymentMethod == PaymentMethod.FREE) {
      throw new IllegalStateException("PG 결제만 완료 처리가 가능합니다.");
    }

    this.pgTid = pgTid;
    this.methodDetail = methodDetail;
    this.paidAt = LocalDateTime.now();
    publishPaymentCompletedEvent();
  }

  /**
   * 빌링 결제 완료 처리 (정기결제)
   *
   * @param pgTid PG 거래 ID
   * @param methodDetail 결제 수단 상세
   * @param billingKey 빌링키
   */
  public void completeBillingPayment(String pgTid, String methodDetail, String billingKey) {
    if (this.paymentMethod != PaymentMethod.BILLING) {
      throw new IllegalStateException("빌링 결제만 완료 처리가 가능합니다.");
    }

    this.pgTid = pgTid;
    this.methodDetail = methodDetail;
    this.billingKey = billingKey;
    this.paidAt = LocalDateTime.now();
    publishPaymentCompletedEvent();
  }

  /**
   * 결제 취소 처리
   *
   * @param reason 취소 사유
   * @param cancelledAmount 취소 금액
   */
  public void cancelPayment(String reason, PaymentAmount cancelledAmount) {
    if (this.paidAt == null) {
      throw new IllegalStateException("완료되지 않은 결제는 취소할 수 없습니다.");
    }

    publishPaymentCancelledEvent(reason, cancelledAmount);
  }

  /**
   * 결제 금액을 반환합니다.
   *
   * @return 결제 금액
   */
  public BigDecimal getPrice() {
    return amount != null ? amount.getValue() : BigDecimal.ZERO;
  }

  /**
   * 결제가 완료되었는지 확인합니다.
   *
   * @return 완료된 경우 true
   */
  public boolean isCompleted() {
    return paidAt != null;
  }

  /**
   * 빌링키 존재 여부 확인
   *
   * @return 빌링키가 있는 경우 true
   */
  public boolean hasBillingKey() {
    return billingKey != null && !billingKey.isEmpty();
  }

  // 이벤트 발행 메서드들

  public void publishPaymentCreatedEvent() {
    PaymentCreatedEvent event =
        new PaymentCreatedEvent(
            this.id,
            this.order != null ? this.order.getId() : null,
            this.amount != null ? this.amount.getValue() : BigDecimal.ZERO,
            this.paymentMethod,
            this.purchaserName,
            this.purchaserEmail,
            this.paymentKey);
    publishEvent(event);
  }

  private void publishPaymentCompletedEvent() {
    PaymentCompletedEvent event =
        new PaymentCompletedEvent(
            this.id,
            this.order != null ? this.order.getId() : null,
            this.amount != null ? this.amount.getValue() : BigDecimal.ZERO,
            this.paymentMethod,
            this.purchaserName,
            this.purchaserEmail,
            this.paymentKey,
            this.pgTid,
            this.methodDetail,
            this.paidAt);
    publishEvent(event);
  }

  private void publishPaymentCancelledEvent(String reason, PaymentAmount cancelledAmount) {
    PaymentCancelledEvent event =
        new PaymentCancelledEvent(
            this.id,
            this.order != null ? this.order.getId() : null,
            this.amount != null ? this.amount.getValue() : BigDecimal.ZERO,
            cancelledAmount != null ? cancelledAmount.getValue() : BigDecimal.ZERO,
            this.paymentMethod,
            this.purchaserName,
            this.purchaserEmail,
            this.paymentKey,
            reason,
            LocalDateTime.now());
    publishEvent(event);
  }

  public enum PaymentMethod {
    FREE("무료"),
    CARD("신용카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    BILLING("정기결제");

    private final String description;

    PaymentMethod(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
