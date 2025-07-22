package liaison.groble.application.payment.exception;

/** 결제 권한 없음 예외 */
public class UnauthorizedPaymentException extends PaymentException {
  public UnauthorizedPaymentException(String message) {
    super(message);
  }
}
