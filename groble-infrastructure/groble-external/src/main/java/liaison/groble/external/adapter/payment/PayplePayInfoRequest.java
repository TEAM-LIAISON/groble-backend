package liaison.groble.external.adapter.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayplePayInfoRequest {
  private String payType; // 결제수단 (transfer|card)
  private String payOid; // 주문번호
  private String payDate; // 원거래 결제일자
}
