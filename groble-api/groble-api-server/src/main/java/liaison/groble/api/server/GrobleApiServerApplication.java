package liaison.groble.api.server;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(
    scanBasePackages = {
      "liaison.groble.api", // API 서버 모듈
      "liaison.groble.application", // 애플리케이션 모듈
      "liaison.groble.domain", // 도메인 모듈
      "liaison.groble.security", // 보안 모듈
      "liaison.groble.persistence", // 인프라스트럭처 - 영속성 모듈
      "liaison.groble.external", // 인프라스트럭처 - 외부 연동 모듈
      "liaison.groble.common", // 공통 모듈
      "liaison.groble.mapping" // 매핑 모듈
    })
@EntityScan(basePackages = {"liaison.groble.domain"})
@EnableJpaRepositories(
    basePackages = {"liaison.groble.persistence", "liaison.groble.domain.user.repository"})
@EnableJpaAuditing
@EnableScheduling
@OpenAPIDefinition
@EnableAsync
@EnableRetry
public class GrobleApiServerApplication {

  public static void main(String[] args) {
    // 애플리케이션 시작 시 JVM 기본 타임존을 Asia/Seoul로 설정
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

    // Spring 애플리케이션 실행 및 컨텍스트 받기
    ApplicationContext context = SpringApplication.run(GrobleApiServerApplication.class, args);

    // 프록시 설정 적용
    setGlobalProxy(context.getEnvironment());
  }

  private static void setGlobalProxy(Environment env) {
    String proxyEnabled = env.getProperty("http.proxy.enabled", "false");

    if ("true".equals(proxyEnabled)) {
      String proxyHost = env.getProperty("http.proxy.host", "10.0.1.238");
      String proxyPort = env.getProperty("http.proxy.port", "3128");

      log.info("=== 전역 프록시 설정 시작 ===");
      log.info("프록시 서버: {}:{}", proxyHost, proxyPort);

      // HTTP 프록시 설정
      System.setProperty("http.proxyHost", proxyHost);
      System.setProperty("http.proxyPort", proxyPort);

      // HTTPS 프록시 설정
      System.setProperty("https.proxyHost", proxyHost);
      System.setProperty("https.proxyPort", proxyPort);

      // 프록시를 거치지 않을 호스트 설정
      System.setProperty(
          "http.nonProxyHosts",
          "localhost|127.0.0.1|10.*|172.16.*|172.31.*|*.groble.im|*.rds.amazonaws.com|*.cache.amazonaws.com|smtp.gmail.com|*.gmail.com");

      // SMTP용 추가 설정 (JavaMail이 참조할 수 있도록)
      System.setProperty("mail.smtp.proxy.host", "");
      System.setProperty("mail.smtp.proxy.port", "");

      log.info("전역 프록시 설정 완료");
      log.info("http.proxyHost: {}", System.getProperty("http.proxyHost"));
      log.info("https.proxyHost: {}", System.getProperty("https.proxyHost"));
      log.info("http.nonProxyHosts: {}", System.getProperty("http.nonProxyHosts"));

      testProxyConnection();
    } else {
      log.info("프록시 설정 비활성화 상태");
    }
  }

  /** 프록시 설정 후 간단한 연결 테스트 */
  private static void testProxyConnection() {
    try {
      java.net.URL url = new java.net.URL("https://www.google.com");
      java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
      connection.setRequestMethod("HEAD");
      connection.setConnectTimeout(60000);
      connection.setReadTimeout(60000);

      int responseCode = connection.getResponseCode();
      connection.disconnect();

      if (responseCode >= 200 && responseCode < 400) {
        log.info("✅ 프록시 연결 테스트 성공 (응답코드: {})", responseCode);
      } else {
        log.warn("⚠️ 프록시 연결 테스트 실패 (응답코드: {})", responseCode);
      }
    } catch (Exception e) {
      log.error("❌ 프록시 연결 테스트 실패: {}", e.getMessage());
    }
  }
}
