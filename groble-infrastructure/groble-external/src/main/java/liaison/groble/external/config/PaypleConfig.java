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

  public String getLinkApiUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/link/api/LinkRegAct.php?ACT_=LINKREG"
        : "https://cpay.payple.kr/php/link/api/LinkRegAct.php?ACT_=LINKREG";
  }

  public String getAppCardPaymentUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/PayCardConfirmAct.php?ACT_=PAYM"
        : "https://cpay.payple.kr/php/PayCardConfirmAct.php?ACT_=PAYM";
  }

  public String getPayConfirmUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/PayConfirmAct.php"
        : "https://cpay.payple.kr/php/PayConfirmAct.php";
  }

  public String getRefererUrl() {
    return testMode ? "https://test.groble.im" : "https://groble.im";
  }

  public String getSimplePaymentUrl() {
    return testMode
        ? "https://democpay.payple.kr/php/SimplePayCardAct.php?ACT_=PAYM"
        : "https://cpay.payple.kr/php/SimplePayCardAct.php?ACT_=PAYM";
  }
}
