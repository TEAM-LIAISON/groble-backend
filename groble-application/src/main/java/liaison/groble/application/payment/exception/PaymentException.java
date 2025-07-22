package liaison.groble.application.payment.exception;

/** 결제 관련 기본 예외 */
public class PaymentException extends RuntimeException {
  public PaymentException(String message) {
    super(message);
  }

  public PaymentException(String message, Throwable cause) {
    super(message, cause);
  }
}
