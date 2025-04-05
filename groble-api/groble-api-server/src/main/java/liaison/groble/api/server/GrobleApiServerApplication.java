package liaison.groble.api.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {
      "liaison.groble.api", // API 서버 모듈
      "liaison.groble.application", // 애플리케이션 모듈
      "liaison.groble.domain", // 도메인 모듈
      "liaison.groble.security", // 보안 모듈
      "liaison.groble.persistence", // 인프라스트럭처 - 영속성 모듈
      "liaison.groble.external", // 인프라스트럭처 - 외부 연동 모듈
      "liaison.groble.common" // 공통 모듈
    })
@EntityScan(basePackages = {"liaison.groble.domain"})
@EnableJpaRepositories(
    basePackages = {"liaison.groble.persistence", "liaison.groble.domain.user.repository"})
@EnableJpaAuditing
public class GrobleApiServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(GrobleApiServerApplication.class, args);
  }
}
