package liaison.groble.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 비즈뿌리오 API 설정 관리 클래스
 *
 * <p>application.yml의 설정값을 자동으로 바인딩합니다. 환경별(개발/운영) 설정을 쉽게 관리할 수 있도록 구조화했습니다.
 */
@Data
@Component
@ConfigurationProperties(prefix = "bizppurio")
public class BizppurioConfig {
  private String baseUrl = "https://api.bizppurio.com";
  private String account;
  private String password;

  // 토큰 갱신 여유 시간 (분 단위)
  private int tokenRefreshMarginMinutes = 60;

  // HTTP 연결 타임아웃 설정 (밀리초)
  private int connectTimeout = 5000;
  private int readTimeout = 30000;
}
