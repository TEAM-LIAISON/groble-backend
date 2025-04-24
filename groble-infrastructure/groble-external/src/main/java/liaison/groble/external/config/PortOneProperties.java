package liaison.groble.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {

  /** V1 API URL (레거시) */
  private String apiUrl = "https://api.iamport.kr";

  /** V2 API URL */
  private String apiUrlV2 = "https://api.portone.io";

  /** 상점 식별자 (기존 가맹점 식별코드) */
  private String merchantId;

  /** API 키 (V1) */
  private String apiKey;

  /** API 시크릿 키 (V1) */
  private String apiSecret;

  /** API 시크릿 키 (V2) */
  private String secretKey;

  /** 클라이언트 키 (V2) - JavaScript SDK 연동용 */
  private String clientKey;

  /** 기본 PG 설정 - V2에서는 pgProvider로 변경 */
  private String defaultPg = "tosspayments";

  /** 결제 완료 후 리다이렉트 URL 기본 주소 */
  private String paymentRedirectUrl = "https://groble.im/payment";

  /** 본인인증 완료 후 리다이렉트 URL 기본 주소 */
  private String identityRedirectUrl = "https://groble.im/identity/verify";

  /** 은행 계좌 검증 콜백 URL */
  private String bankCallbackUrl = "https://groble.im/bank/callback";

  /** 웹훅 비밀 키 (웹훅 서명 검증용) */
  private String webhookSecret;
}
