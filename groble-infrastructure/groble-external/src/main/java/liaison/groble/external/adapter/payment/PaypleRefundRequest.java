package liaison.groble.external.adapter.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleRefundRequest {
  private String payOid; // 주문번호
  private String payDate; // 원거래 결제일자
  private String refundTotal; // 결제취소 요청금액
  private String refundTaxtotal; // 결제취소 부가세
}
