package liaison.groble.application.payment.exception;

import liaison.groble.common.exception.GrobleException;

public class PayplePaymentAuthException extends GrobleException {
  public PayplePaymentAuthException(String message) {
    super(message, 422);
  }
}
