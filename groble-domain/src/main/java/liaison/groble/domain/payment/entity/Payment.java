package liaison.groble.domain.payment.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.order.entity.Order;

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
      @Index(name = "idx_payment_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 주문 정보 (1:1 관계) */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  /** 결제 금액 - 실제 결제된 금액 */
  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

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
    this.price = price != null ? price : BigDecimal.ZERO;
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
   * @param price 결제 금액
   * @param paymentMethod 결제 수단
   * @param paymentKey PG 결제 키
   * @return PG 결제 엔티티
   */
  public static Payment createPgPayment(
      Order order, BigDecimal price, PaymentMethod paymentMethod, String paymentKey) {
    return Payment.builder()
        .order(order)
        .price(price)
        .paymentMethod(paymentMethod)
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
  }

  public enum PaymentMethod {
    FREE("무료"),
    CARD("신용카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌");

    private final String description;

    PaymentMethod(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public void cancel() {
    //
  }
}
