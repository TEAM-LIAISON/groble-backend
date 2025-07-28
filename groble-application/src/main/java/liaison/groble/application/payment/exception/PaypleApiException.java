package liaison.groble.application.payment.exception;

/** 페이플 API 호출 실패 예외 */
public class PaypleApiException extends PaymentException {
  public PaypleApiException(String message) {
    super(message);
  }

  public PaypleApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
