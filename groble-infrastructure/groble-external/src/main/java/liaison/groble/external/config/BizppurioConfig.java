package liaison.groble.external.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import lombok.Data;

/**
 * 비즈뿌리오 API 설정 관리 클래스
 *
 * <p>application.yml의 설정값을 자동으로 바인딩합니다.
 */
@Data
@Configuration
@EnableRetry
@ConfigurationProperties(prefix = "bizppurio")
public class BizppurioConfig {
  private String baseUrl;
  private String account;
  private String password;

  // 토큰 관련 설정
  private int tokenRefreshMarginMinutes = 60;

  // HTTP 연결 타임아웃 설정 (밀리초)
  private int connectTimeout = 5000;
  private int readTimeout = 30000;

  // 메시지 발송 설정
  private String defaultSender;
  private String defaultCountry = "82";

  // 알림톡/친구톡 설정
  private String kakaoSenderKey; // 카카오톡 발신프로필키

  private Map<String, Template> templates = new HashMap<>();

  // 재시도 설정
  private int maxRetryAttempts = 3;
  private long retryDelay = 1000; // 밀리초

  @Data
  public static class Template {
    private String code;
    private String name;
  }
}
