package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppCardPayplePaymentDTO {
  private String payRst; // 결제 결과 (success/error)

  private String payCode; // 결제 결과 코드

  private String payMsg; // 결제 결과 메시지

  private String payOid; // 주문번호

  private String payType; // 결제 방법 (card)

  private String payTime; // 결제 완료 시간

  private String payTotal; // 결제 금액

  private String payCardName; // 카드사명

  private String payCardNum; // 카드번호 (마스킹)

  private String payCardQuota; // 할부개월수

  private String payCardTradeNum; // 거래번호

  private String payCardAuthNo; // 승인번호
  private String payCardReceipt; // 카드 매출전표 URL
}
