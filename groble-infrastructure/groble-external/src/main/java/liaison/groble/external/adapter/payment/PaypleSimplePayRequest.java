package liaison.groble.external.adapter.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleSimplePayRequest {
  private String payType; // 결제수단 (card|transfer)
  private String payerId; // 결제자 고유 ID (빌링키)
  private String payGoods; // 상품명
  private String payTotal; // 결제요청금액
  private String payOid; // 주문번호
  private String payerNo; // 결제자 고유번호
  private String payerName; // 결제자 이름
  private String payerHp; // 결제자 휴대전화번호
  private String payerEmail; // 결제자 이메일
  private String payIstax; // 과세여부
  private String payTaxtotal; // 부가세
  private String simpleFlag; // 정기결제 여부 (Y/N)
}
