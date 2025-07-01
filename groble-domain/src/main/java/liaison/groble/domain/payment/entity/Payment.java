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
import jakarta.persistence.Version;

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
      @Index(name = "idx_payment_status", columnList = "status"),
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

  /** 결제 상태 */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status = PaymentStatus.READY;

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

  /** 취소 사유 */
  @Column(name = "cancel_reason")
  private String cancelReason;

  /** 취소 시각 */
  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  /** 실패 사유 */
  @Column(name = "fail_reason")
  private String failReason;

  /** 낙관적 락 버전 */
  @Version private Long version;

  // 생성자
  @Builder
  public Payment(
      Order order,
      BigDecimal price,
      PaymentMethod paymentMethod,
      PaymentStatus status,
      String paymentKey,
      String purchaserName,
      String purchaserEmail,
      String purchaserPhoneNumber) {
    this.order = order;
    this.price = price != null ? price : BigDecimal.ZERO;
    this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.FREE;
    this.status = status != null ? status : PaymentStatus.READY;
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
        .status(PaymentStatus.IN_PROGRESS)
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

    this.status = PaymentStatus.PAID;
    this.paidAt = LocalDateTime.now();

    // 주문 상태도 함께 업데이트
    this.order.completePayment();
  }

  /**
   * PG 결제 완료 처리
   *
   * @param pgTid PG사 거래번호
   * @param methodDetail 결제 수단 상세 정보
   */
  public void completePgPayment(String pgTid, String methodDetail) {
    if (this.status != PaymentStatus.READY && this.status != PaymentStatus.IN_PROGRESS) {
      throw new IllegalStateException("결제 준비 또는 진행 상태에서만 결제 완료 처리가 가능합니다.");
    }

    this.status = PaymentStatus.PAID;
    this.pgTid = pgTid;
    this.methodDetail = methodDetail;
    this.paidAt = LocalDateTime.now();

    // 주문 상태도 함께 업데이트
    this.order.completePayment();
  }

  /**
   * 결제 실패 처리
   *
   * @param failReason 실패 사유
   */
  public void markAsFailed(String failReason) {
    if (this.status == PaymentStatus.PAID || this.status == PaymentStatus.CANCELLED) {
      throw new IllegalStateException("이미 완료되거나 취소된 결제는 실패 처리할 수 없습니다.");
    }

    this.status = PaymentStatus.FAILED;
    this.failReason = failReason;

    // 주문 상태도 함께 업데이트
    this.order.failOrder("결제 실패: " + failReason);
  }

  /**
   * 결제 취소 처리
   *
   * @param reason 취소 사유
   */
  public void cancel(String reason) {
    if (this.status != PaymentStatus.PAID) {
      throw new IllegalStateException("결제 완료 상태에서만 취소가 가능합니다.");
    }

    this.status = PaymentStatus.CANCELLED;
    this.cancelReason = reason;
    this.cancelledAt = LocalDateTime.now();
  }

  /** 결제 진행중 상태로 변경 */
  public void markAsInProgress() {
    if (this.status != PaymentStatus.READY) {
      throw new IllegalStateException("결제 준비 상태에서만 진행중으로 변경 가능합니다.");
    }
    this.status = PaymentStatus.IN_PROGRESS;
  }

  public void markAsPaid() {
    if (this.status != PaymentStatus.IN_PROGRESS) {
      throw new IllegalStateException("결제 진행중 상태에서만 완료로 변경 가능합니다.");
    }
    this.status = PaymentStatus.PAID;
    this.paidAt = LocalDateTime.now();
  }

  // 편의 메서드들
  public boolean isPaid() {
    return status == PaymentStatus.PAID;
  }

  public boolean isFreePayment() {
    return paymentMethod == PaymentMethod.FREE;
  }

  public boolean isCancelled() {
    return status == PaymentStatus.CANCELLED;
  }

  public boolean isFailed() {
    return status == PaymentStatus.FAILED;
  }

  // 열거형 정의
  public enum PaymentStatus {
    READY("결제준비"),
    IN_PROGRESS("결제진행중"),
    PAID("결제완료"),
    CANCELLED("취소됨"),
    FAILED("결제실패");

    private final String description;

    PaymentStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
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
}
