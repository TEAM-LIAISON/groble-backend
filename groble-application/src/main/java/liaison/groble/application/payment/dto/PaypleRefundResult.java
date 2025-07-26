package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

/** 페이플 환불 결과 */
@Getter
@Builder
public class PaypleRefundResult {
  private final boolean success;
  private final String payRst;
  private final String payCode;
  private final String payMsg;
  private final String refundOid;
  private final String refundTotal;
  private final String errorCode;
  private final String errorMessage;
}
