package liaison.groble.application.payment.exception;

/** 페이플 결제 승인 실패 예외 */
public class PayplePaymentApprovalException extends PaymentException {
  public PayplePaymentApprovalException(String message) {
    super(message);
  }

  public PayplePaymentApprovalException(String message, Throwable cause) {
    super(message, cause);
  }
}
