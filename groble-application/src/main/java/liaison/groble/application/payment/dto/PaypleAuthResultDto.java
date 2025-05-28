package liaison.groble.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaypleAuthResultDto {
  @JsonProperty("PCD_PAY_RST")
  private String payRst; // success, error, close

  @JsonProperty("PCD_PAY_CODE")
  private String payCode; // 0000

  @JsonProperty("PCD_PAY_MSG")
  private String payMsg; // 카드인증완료

  @JsonProperty("PCD_PAY_TYPE")
  private String payType; // card, transfer

  @JsonProperty("PCD_CARD_VER")
  private String cardVer; // 02

  @JsonProperty("PCD_PAY_WORK")
  private String payWork; // CERT

  @JsonProperty("PCD_AUTH_KEY")
  private String authKey; // 파트너 인증 키

  @JsonProperty("PCD_PAY_REQKEY")
  private String payReqKey; // 결제 키

  @JsonProperty("PCD_PAY_HOST")
  private String payHost; // 페이플 접속 주소

  @JsonProperty("PCD_PAY_URL")
  private String payUrl;

  @JsonProperty("PCD_PAY_COFURL")
  private String payCofUrl; // 승인 요청 URL

  @JsonProperty("PCD_PAYER_ID")
  private String payerId; // 빌링키

  @JsonProperty("PCD_PAYER_NO")
  private String payerNo; // 회원번호

  @JsonProperty("PCD_PAYER_NAME")
  private String payerName; // 구매자 이름

  @JsonProperty("PCD_PAYER_HP")
  private String payerHp; // 구매자 휴대폰번호

  @JsonProperty("PCD_PAYER_EMAIL")
  private String payerEmail; // 구매자 이메일

  @JsonProperty("PCD_PAY_OID")
  private String payOid; // 주문번호

  @JsonProperty("PCD_PAY_GOODS")
  private String payGoods; // 상품명

  @JsonProperty("PCD_PAY_TOTAL")
  private String payTotal; // 총 결제금액

  @JsonProperty("PCD_PAY_TAXTOTAL")
  private String payTaxTotal; // 복합과세 부가세

  @JsonProperty("PCD_PAY_ISTAX")
  private String payIsTax; // 과세 여부

  @JsonProperty("PCD_PAY_CARDNAME")
  private String payCardName; // 카드사명

  @JsonProperty("PCD_PAY_CARDNUM")
  private String payCardNum; // 카드번호

  @JsonProperty("PCD_PAY_CARDQUOTA")
  private String payCardQuota; // 할부 개월수

  @JsonProperty("PCD_PAY_CARDTRADENUM")
  private String payCardTradeNum; // 거래 고유 키

  @JsonProperty("PCD_PAY_CARDAUTHNO")
  private String payCardAuthNo; // 승인번호

  @JsonProperty("PCD_PAY_CARDRECEIPT")
  private String payCardReceipt; // 매출전표 URL

  @JsonProperty("PCD_PAY_TIME")
  private String payTime; // 결제 요청 시간

  @JsonProperty("PCD_REGULER_FLAG")
  private String regulerFlag; // 월 중복방지 거래 설정

  @JsonProperty("PCD_PAY_YEAR")
  private String payYear; // 년

  @JsonProperty("PCD_PAY_MONTH")
  private String payMonth; // 월

  @JsonProperty("PCD_SIMPLE_FLAG")
  private String simpleFlag; // 간편결제 설정값

  @JsonProperty("PCD_RST_URL")
  private String rstUrl; // 결과 전송 URL

  @JsonProperty("PCD_PAY_AMOUNT")
  private String payAmount;

  @JsonProperty("PCD_PAY_DISCOUNT")
  private String payDiscount;

  @JsonProperty("PCD_PAY_AMOUNT_REAL")
  private String payAmountReal;

  @JsonProperty("PCD_USER_DEFINE1")
  private String userDefine1;

  @JsonProperty("PCD_USER_DEFINE2")
  private String userDefine2;

  public boolean isSuccess() {
    return "success".equals(payRst);
  }

  public boolean isError() {
    return "error".equals(payRst);
  }

  public boolean isClosed() {
    return "close".equals(payRst);
  }
}
