package liaison.groble.application.payment.exception.refund;

import liaison.groble.application.payment.exception.PaymentException;

public class PaymentRefundBadRequestException extends PaymentException {
  private final String contentType;

  public PaymentRefundBadRequestException(String contentType) {
    super("[" + contentType + "] 콘텐츠는 환불이 허용되지 않습니다.");
    this.contentType = contentType;
  }

  public String getContentType() {
    return contentType;
  }
}
