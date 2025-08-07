package liaison.groble.application.payment.exception;

/** 페이플 환불 실패 예외 */
public class PaypleRefundException extends PaymentException {
  public PaypleRefundException(String message) {
    super(message);
  }

  public PaypleRefundException(String message, Throwable cause) {
    super(message, cause);
  }
}
