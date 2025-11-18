package liaison.groble.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payple")
public class PaypleConfig {

  private String cstId; // 파트너사 ID
  private String custKey; // 파트너사 키
  private String refundKey; // 환불키
  private String clientKey; // 클라이언트 키
  private String authUrl; // 파트너 인증 URL
  private boolean testMode; // 테스트 모드 여부
  private String testTransferAmount = "1000"; // 테스트 모드 시 이체 금액 (기본값: 1000원)

  // URL 설정
  public String getPaymentJsUrl() {
    return testMode
        ? "https://democpay.payple.kr/js/v1/payment.js"
        : "https://cpay.payple.kr/js/v1/payment.js";
  }

  public String getAuthApiUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/auth.php"
        : "https://cpay.payple.kr/php/auth.php";
  }

  public String getAppCardPaymentUrl() {
    return testMode
        ? "https://demo-api-v2.payple.kr/api/v1/payments/cards/approval/confirm"
        : "https://api-v2.payple.kr/api/v1/payments/cards/approval/confirm";
  }

  public String getPayConfirmUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/PayConfirmAct.php"
        : "https://cpay.payple.kr/php/PayConfirmAct.php";
  }

  public String getRefererUrl() {
    return testMode ? "https://dev.groble.im" : "https://groble.im";
  }

  public String getSimplePaymentUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/SimplePayCardAct.php?ACT_=PAYM"
        : "https://cpay.payple.kr/php/SimplePayCardAct.php?ACT_=PAYM";
  }

  public String getBillingKeyDeleteUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/cPayUser/api/cPayUserAct.php?ACT_=PUSERDEL"
        : "https://cpay.payple.kr/php/cPayUser/api/cPayUserAct.php?ACT_=PUSERDEL";
  }

  public String getCancelApiUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/account/api/cPayCAct.php"
        : "https://cpay.payple.kr/php/account/api/cPayCAct.php";
  }

  public String getTransferCancelUrl() {
    return testMode
        ? "https://demohub.payple.kr/transfer/cancel"
        : "https://hub.payple.kr/transfer/cancel";
  }

  // 정산지급대행 URL 설정
  public String getSettlementAuthApiUrl() {
    return testMode ? "https://demohub.payple.kr/oauth/token" : "https://hub.payple.kr/oauth/token";
  }

  // 계좌인증 요청 URL
  public String getAccountVerificationUrl() {
    return testMode
        ? "https://demohub.payple.kr/inquiry/real_name"
        : "https://hub.payple.kr/inquiry/real_name";
  }

  // 빌링키로 이체 대기 요청
  public String getPendingTransferRequestUrl() {
    return testMode
        ? "https://demohub.payple.kr/transfer/request"
        : "https://hub.payple.kr/transfer/request";
  }

  // 이체 실행 요청
  public String getTransferExecuteUrl() {
    return testMode
        ? "https://demohub.payple.kr/transfer/execute"
        : "https://hub.payple.kr/transfer/execute";
  }

  // 웹훅 URL 설정
  public String getWebhookUrl() {
    return testMode
        ? "https://api.dev.groble.im/api/webhooks/payple/transfer-result"
        : "https://api.groble.im/api/webhooks/payple/transfer-result";
  }

  // 이체 가능 잔액 조회 URL
  public String getAccountRemainUrl() {
    return testMode
        ? "https://demohub.payple.kr/account/remain"
        : "https://hub.payple.kr/account/remain";
  }
}
