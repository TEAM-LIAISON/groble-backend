package liaison.groble.application.payment.exception;

/** 결제 처리 중 일반 오류 예외 */
public class PaymentProcessingException extends PaymentException {
  public PaymentProcessingException(String message) {
    super(message);
  }

  public PaymentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
