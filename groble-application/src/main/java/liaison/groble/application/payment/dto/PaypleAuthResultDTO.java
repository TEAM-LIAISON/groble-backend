package liaison.groble.application.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaypleAuthResultDTO {

  private String payRst; // success, error, close
  private String pcdPayMethod;
  private String payCode; // 0000
  private String payMsg; // 카드인증완료
  private String payType; // card, transfer
  private String cardVer; // 02
  private String payWork; // CERT
  private String authKey; // 파트너 인증 키
  private String payReqKey; // 결제 키
  private String payHost; // 페이플 접속 주소
  private String payCofUrl; // 승인 요청 URL
  private String payDiscount; // 할인 금액
  private String payEasyPayMethod; // 선택한 결제 수단의 상세 유형
  private String easyPayMethod;
  private String payerNo; // 회원번호
  private String payAmount; // 결제금액
  private String payAmountReal; // 실제 결제금액
  private String payerName; // 구매자 이름
  private String payerHp; // 구매자 휴대폰번호
  private String payerId; // 구매자 아이디
  private String payerEmail; // 구매자 이메일
  private String payOid; // 주문번호
  private String payGoods; // 상품명
  private String payTotal; // 총 결제금액
  private String payTaxTotal; // 복합과세 부가세
  private String payIsTax; // 과세 여부
  private String payCardName; // 카드사명
  private String payCardNum; // 카드번호
  private String payCardQuota;
  private String payCardTradeNum; // 거래 고유 키
  private String payCardAuthNo; // 승인번호
  private String payCardReceipt; // 매출전표 URL
  private String payTime; // 결제 요청 시간
  private String regulerFlag; // 월 중복방지 거래 설정
  private String payYear; // 년
  private String payMonth; // 월
  private String simpleFlag; // 간편결제 설정값
  private String rstUrl; // 결과 전송 URL
  private String userDefine1;
  private String userDefine2;
  private String pcdPayUrl;
}
