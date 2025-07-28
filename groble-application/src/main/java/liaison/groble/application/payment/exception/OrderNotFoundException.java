package liaison.groble.application.payment.exception;

/** 주문을 찾을 수 없음 예외 */
public class OrderNotFoundException extends PaymentException {
  public OrderNotFoundException(String message) {
    super(message);
  }
}
