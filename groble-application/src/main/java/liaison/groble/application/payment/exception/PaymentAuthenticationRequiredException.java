package liaison.groble.application.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 결제 처리를 위한 인증이 필요할 때 발생하는 예외 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class PaymentAuthenticationRequiredException extends RuntimeException {

  private static final String MESSAGE_PAYMENT = "결제 처리를 위해서는 회원 로그인 또는 비회원 인증이 필요합니다.";
  private static final String MESSAGE_CANCEL = "결제 취소를 위해서는 회원 로그인 또는 비회원 인증이 필요합니다.";

  public PaymentAuthenticationRequiredException(String message) {
    super(message);
  }

  public static PaymentAuthenticationRequiredException forPayment() {
    return new PaymentAuthenticationRequiredException(MESSAGE_PAYMENT);
  }

  public static PaymentAuthenticationRequiredException forCancel() {
    return new PaymentAuthenticationRequiredException(MESSAGE_CANCEL);
  }
}
