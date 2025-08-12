package liaison.groble.api.model.payment.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "PaypleAuthResultRequest", description = "Payple 인증 결과 요청 모델")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaypleAuthResultRequest {
  @Schema(description = "결과 상태 (success, error, close)", example = "success")
  @JsonProperty("PCD_PAY_RST")
  private String payRst;

  @Schema(description = "결제 수단 코드", example = "CARD")
  @JsonProperty("PCD_PAY_METHOD")
  private String pcdPayMethod;

  @Schema(description = "결제 결과 코드", example = "0000")
  @JsonProperty("PCD_PAY_CODE")
  private String payCode;

  @Schema(description = "결제 결과 메시지", example = "카드인증완료")
  @JsonProperty("PCD_PAY_MSG")
  private String payMsg;

  @Schema(description = "결제 타입 (card, transfer 등)", example = "card")
  @JsonProperty("PCD_PAY_TYPE")
  private String payType;

  @Schema(description = "카드 버전", example = "02")
  @JsonProperty("PCD_CARD_VER")
  private String cardVer;

  @Schema(description = "작업 코드 (CERT 등)", example = "CERT")
  @JsonProperty("PCD_PAY_WORK")
  private String payWork;

  @Schema(description = "파트너 인증 키", example = "AUTH_KEY_123")
  @JsonProperty("PCD_AUTH_KEY")
  private String authKey;

  @Schema(description = "결제 요청 키", example = "REQ_KEY_456")
  @JsonProperty("PCD_PAY_REQKEY")
  private String payReqKey;

  @Schema(description = "결제 요청 시간", example = "REQ_KEY_456")
  @JsonProperty("PCD_PAY_REQ_TIME")
  private String payReqTime;

  @Schema(description = "페이플 접속 호스트 주소", example = "https://payple.com")
  @JsonProperty("PCD_PAY_HOST")
  private String payHost;

  @Schema(description = "승인 요청 URL", example = "https://payple.com/approve")
  @JsonProperty("PCD_PAY_COFURL")
  private String payCofUrl;

  @Schema(description = "할인 금액", example = "1000")
  @JsonProperty("PCD_PAY_DISCOUNT")
  private String payDiscount;

  @Schema(description = "선택한 결제 수단의 상세 유형", example = "card")
  @JsonProperty("PCD_EASY_PAY_METHOD")
  private String easyPayMethod;

  @Schema(description = "선택한 결제 수단의 상세 유형", example = "card")
  @JsonProperty("PCD_PAY_EASY_PAY_METHOD")
  private String payEasyPayMethod;

  @Schema(description = "회원번호", example = "12345")
  @JsonProperty("PCD_PAYER_NO")
  private String payerNo;

  @Schema(description = "결제금액", example = "50000")
  @JsonProperty("PCD_PAY_AMOUNT")
  private String payAmount;

  @Schema(description = "실제 결제금액", example = "49000")
  @JsonProperty("PCD_PAY_AMOUNT_REAL")
  private String payAmountReal;

  @Schema(description = "구매자 이름", example = "홍길동")
  @JsonProperty("PCD_PAYER_NAME")
  private String payerName;

  @Schema(description = "구매자 휴대폰번호", example = "010-1234-5678")
  @JsonProperty("PCD_PAYER_HP")
  private String payerHp;

  @Schema(description = "구매자 아이디", example = "hongildong")
  @JsonProperty("PCD_PAYER_ID")
  private String payerId;

  @Schema(description = "구매자 이메일", example = "hong@example.com")
  @JsonProperty("PCD_PAYER_EMAIL")
  private String payerEmail;

  @Schema(description = "주문번호", example = "OID123456789")
  @JsonProperty("PCD_PAY_OID")
  private String payOid;

  @Schema(description = "상품명", example = "고급 코딩 강의")
  @JsonProperty("PCD_PAY_GOODS")
  private String payGoods;

  @Schema(description = "총 결제금액", example = "55000")
  @JsonProperty("PCD_PAY_TOTAL")
  private String payTotal;

  @Schema(description = "부가세", example = "5000")
  @JsonProperty("PCD_PAY_TAXTOTAL")
  private String payTaxTotal;

  @Schema(description = "과세 여부 (Y/N)", example = "Y")
  @JsonProperty("PCD_PAY_ISTAX")
  private String payIsTax;

  @Schema(description = "카드사명", example = "신한카드")
  @JsonProperty("PCD_PAY_CARDNAME")
  private String payCardName;

  @Schema(description = "카드번호", example = "1234-****-****-5678")
  @JsonProperty("PCD_PAY_CARDNUM")
  private String payCardNum;

  @Schema(description = "할부 개월 수", example = "0")
  @JsonProperty("PCD_PAY_CARDQUOTA")
  private String payCardQuota;

  @Schema(description = "거래 고유 키", example = "TRADE123456")
  @JsonProperty("PCD_PAY_CARDTRADENUM")
  private String payCardTradeNum;

  @Schema(description = "승인번호", example = "AUTH987654")
  @JsonProperty("PCD_PAY_CARDAUTHNO")
  private String payCardAuthNo;

  @Schema(description = "매출전표 URL", example = "https://payple.com/receipt/123")
  @JsonProperty("PCD_PAY_CARDRECEIPT")
  private String payCardReceipt;

  @Schema(description = "결제 요청 시간 (YYYYMMDDHHMMSS)", example = "20250626123045")
  @JsonProperty("PCD_PAY_TIME")
  private String payTime;

  @Schema(description = "월 중복방지 거래 설정 (Y/N)", example = "N")
  @JsonProperty("PCD_REGULER_FLAG")
  private String regulerFlag;

  @Schema(description = "결제 연도", example = "2025")
  @JsonProperty("PCD_PAY_YEAR")
  private String payYear;

  @Schema(description = "결제 월", example = "06")
  @JsonProperty("PCD_PAY_MONTH")
  private String payMonth;

  @Schema(description = "간편결제 설정값", example = "Y")
  @JsonProperty("PCD_SIMPLE_FLAG")
  private String simpleFlag;

  @Schema(description = "결과 전송 URL", example = "https://yourapp.com/payment/result")
  @JsonProperty("PCD_RST_URL")
  private String rstUrl;

  @Schema(description = "결과 전송 URL", example = "/payment-result")
  @JsonProperty("resultUrl")
  private String resultUrl;

  @Schema(description = "사용자 정의 필드1", example = "custom1")
  @JsonProperty("PCD_USER_DEFINE1")
  private String userDefine1;

  @Schema(description = "사용자 정의 필드2", example = "custom2")
  @JsonProperty("PCD_USER_DEFINE2")
  private String userDefine2;

  @Schema(description = "결제 페이지 URL", example = "https://payple.com/pay")
  @JsonProperty("PCD_PAY_URL")
  private String pcdPayUrl;
}
