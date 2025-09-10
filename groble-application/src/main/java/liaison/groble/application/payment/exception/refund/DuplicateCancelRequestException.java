package liaison.groble.application.payment.exception.refund;

import liaison.groble.application.payment.exception.PaymentException;

import lombok.Getter;

/** 중복 취소 요청에 대한 예외 클래스 이미 취소 요청된 주문에 대해 재요청이 올 경우 발생 */
@Getter
public class DuplicateCancelRequestException extends PaymentException {
  private final String orderId;
  private final String orderStatus;

  public DuplicateCancelRequestException(String message, String orderId, String orderStatus) {
    super(message);
    this.orderId = orderId;
    this.orderStatus = orderStatus;
  }
}
