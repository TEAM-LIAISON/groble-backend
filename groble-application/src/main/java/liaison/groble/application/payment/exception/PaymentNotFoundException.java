package liaison.groble.application.payment.exception;

/** 결제 정보를 찾을 수 없음 예외 */
public class PaymentNotFoundException extends PaymentException {
  public PaymentNotFoundException(String message) {
    super(message);
  }
}
