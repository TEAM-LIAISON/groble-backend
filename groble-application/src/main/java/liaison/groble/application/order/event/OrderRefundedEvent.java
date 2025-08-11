package liaison.groble.application.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

import liaison.groble.domain.order.entity.Order;

import lombok.Getter;

/** 주문 환불 이벤트 */
@Getter
public class OrderRefundedEvent extends ApplicationEvent {
  private final Order order;
  private final BigDecimal refundAmount;
  private final boolean isPartial;
  private final String reason;
  private final LocalDateTime refundedAt;

  public OrderRefundedEvent(
      Object source, Order order, BigDecimal refundAmount, boolean isPartial, String reason) {
    super(source);
    this.order = order;
    this.refundAmount = refundAmount;
    this.isPartial = isPartial;
    this.reason = reason;
    this.refundedAt = LocalDateTime.now();
  }

  /** 전액 환불 이벤트 생성 */
  public static OrderRefundedEvent fullRefund(Object source, Order order, String reason) {
    return new OrderRefundedEvent(source, order, order.getFinalPrice(), false, reason);
  }

  /** 부분 환불 이벤트 생성 */
  public static OrderRefundedEvent partialRefund(
      Object source, Order order, BigDecimal amount, String reason) {
    return new OrderRefundedEvent(source, order, amount, true, reason);
  }
}
