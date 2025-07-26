package liaison.groble.application.payment.exception;

/** 결제 인증 실패 예외 */
public class PaymentAuthException extends PaymentException {
  public PaymentAuthException(String message) {
    super(message);
  }
}
