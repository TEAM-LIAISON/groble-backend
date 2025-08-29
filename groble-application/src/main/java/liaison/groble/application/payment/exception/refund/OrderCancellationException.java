package liaison.groble.application.payment.exception.refund;

import liaison.groble.application.payment.exception.PaymentException;

import lombok.Getter;

// 커스텀 예외 클래스
@Getter
public class OrderCancellationException extends PaymentException {
  private final String orderId;
  private final String orderStatus;

  public OrderCancellationException(String message, String orderId, String orderStatus) {
    super(message);
    this.orderId = orderId;
    this.orderStatus = orderStatus;
  }
}
