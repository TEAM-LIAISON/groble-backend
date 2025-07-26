package liaison.groble.application.payment.exception;

/** 결제 검증 실패 예외 */
public class PaymentValidationException extends PaymentException {
  public PaymentValidationException(String message) {
    super(message);
  }
}
