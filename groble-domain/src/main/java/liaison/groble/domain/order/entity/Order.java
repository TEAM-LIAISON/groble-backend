package liaison.groble.domain.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.gig.entity.Gig;
import liaison.groble.domain.gig.enums.GigStatus;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.purchase.entity.Purchaser;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "orders",
    indexes = {
      @Index(name = "idx_order_user", columnList = "user_id"),
      @Index(name = "idx_order_merchant_uid", columnList = "merchant_uid", unique = true),
      @Index(name = "idx_order_status", columnList = "status"),
      @Index(name = "idx_order_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "merchant_uid", nullable = false, unique = true)
  private String merchantUid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.PENDING;

  @Column(nullable = false)
  private BigDecimal totalAmount;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Payment payment;

  @Embedded private Purchaser purchaser;

  @Column(name = "order_note", columnDefinition = "TEXT")
  private String orderNote;

  @Version private Long version;

  // 생성자
  @Builder(access = AccessLevel.PACKAGE)
  private Order(User user, BigDecimal totalAmount, Purchaser purchaser, String orderNote) {
    this.user = user;
    this.totalAmount = totalAmount;
    this.purchaser = purchaser;
    this.orderNote = orderNote;
    this.merchantUid = generateMerchantUid();
  }

  // 비즈니스 메서드
  public void completePayment() {
    validateStateTransition(OrderStatus.PAID);
    this.status = OrderStatus.PAID;
  }

  public void cancelOrder(String reason) {
    validateStateTransition(OrderStatus.CANCELLED);
    this.status = OrderStatus.CANCELLED;
    this.orderNote = reason;
  }

  public void failOrder(String reason) {
    validateStateTransition(OrderStatus.FAILED);
    this.status = OrderStatus.FAILED;
    this.orderNote = reason;
  }

  private void validateStateTransition(OrderStatus newStatus) {
    if (this.status == newStatus) {
      return;
    }

    switch (this.status) {
      case PENDING:
        if (newStatus != OrderStatus.PAID
            && newStatus != OrderStatus.CANCELLED
            && newStatus != OrderStatus.FAILED) {
          throw new IllegalStateException("대기 상태에서는 결제완료, 취소, 실패 상태로만 변경 가능합니다.");
        }
        break;
      case PAID:
        if (newStatus != OrderStatus.CANCELLED) {
          throw new IllegalStateException("결제완료 상태에서는 취소 상태로만 변경 가능합니다.");
        }
        break;
      case CANCELLED:
      case FAILED:
        throw new IllegalStateException("이미 종료된 주문은 상태를 변경할 수 없습니다.");
    }
  }

  // 연관관계 편의 메서드 수정
  public void addOrderItem(
      Gig gig,
      BigDecimal price,
      OrderItem.OptionType optionType,
      Long optionId,
      String optionName,
      int quantity) {
    OrderItem orderItem =
        OrderItem.builder()
            .order(this)
            .gig(gig)
            .price(price) // 옵션에 따른 가격 사용
            .quantity(quantity)
            .optionType(optionType)
            .optionId(optionId)
            .optionName(optionName)
            .build();

    this.orderItems.add(orderItem);
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  // 유틸리티 메서드
  private String generateMerchantUid() {
    return "ORD_"
        + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10)
        + "_"
        + System.currentTimeMillis();
  }

  // 팩토리 메서드 수정 (옵션 버전 추가)
  public static Order createOrderWithOption(
      User user,
      Gig gig,
      OrderItem.OptionType optionType,
      Long optionId,
      String optionName,
      BigDecimal price,
      Purchaser purchaser,
      String orderNote) {
    Order order =
        Order.builder()
            .user(user)
            .totalAmount(price) // 옵션 가격으로 초기화
            .purchaser(purchaser)
            .orderNote(orderNote)
            .build();

    // 상품 상태 확인
    if (gig.getStatus() != GigStatus.ACTIVE) {
      throw new IllegalArgumentException("판매중인 상품만 구매할 수 있습니다: " + gig.getTitle());
    }

    // 옵션 정보를 포함한 주문 아이템 추가
    order.addOrderItem(gig, price, optionType, optionId, optionName, 1);

    return order;
  }

  // 내부 클래스
  public enum OrderStatus {
    PENDING("결제대기"),
    PAID("결제완료"),
    CANCELLED("취소됨"),
    FAILED("결제실패");

    private final String description;

    OrderStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
