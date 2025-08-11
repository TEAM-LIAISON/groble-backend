package liaison.groble.application.order.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

import liaison.groble.domain.order.entity.Order;

import lombok.Getter;

/** 주문 결제 완료 이벤트 */
@Getter
public class OrderPaidEvent extends ApplicationEvent {
  private final Order order;
  private final LocalDateTime paidAt;

  public OrderPaidEvent(Object source, Order order) {
    super(source);
    this.order = order;
    this.paidAt = LocalDateTime.now();
  }
}
