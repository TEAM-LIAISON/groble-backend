package liaison.groble.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {

  /** 포트원 API 키 */
  private String apiKey;

  /** 포트원 시크릿 키 */
  private String secretKey;

  /** 포트원 API 기본 URL */
  private String baseUrl = "https://api.portone.io/v2";

  /** 웹훅 시크릿 키 */
  private String webhookSecret;

  /** 기본 PG사 설정 */
  private String defaultPg = "inicis";

  /** 결제 리다이렉트 URL */
  private String paymentRedirectUrl;

  /** 본인인증 리다이렉트 URL */
  private String identityRedirectUrl;

  /** 계좌인증 콜백 URL */
  private String bankCallbackUrl;

  /** 가맹점 식별코드 */
  private String merchantCode;
}
