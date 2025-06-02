package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayplePaymentResultDto {
  private String payRst; // 결제결과 (success|error)
  private String payMsg; // 결제결과 메시지
  private String payOid; // 주문번호
  private String payerId; // 결제자 고유 ID (빌링키)
  private String payTime; // 결제완료 시간
  private String payCardName; // 카드사명
  private String payCardNum; // 카드번호
  private String payBankName; // 은행명
  private String payBankNum; // 계좌번호
}
