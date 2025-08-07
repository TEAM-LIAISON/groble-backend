package liaison.groble.api.server;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

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
    SpringApplication.run(GrobleApiServerApplication.class, args);
  }
}
