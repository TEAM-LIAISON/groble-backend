package liaison.grobleapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(
    basePackages = {
      "liaison.grobleauth",
      "liaison.grobleapi",
      "liaison.groblecommon",
      "liaison.groblecore",
      "liaison.groblepayment",
    })
@EntityScan(
    basePackages = {
      "liaison.groblecore.domain",
      "liaison.grobleauth.domain",
      "liaison.grobleapi.domain",
      "liaison.groblepayment.domain"
    })
@EnableJpaRepositories(
    basePackages = {"liaison.groblecore.repository"
      //                "liaison.grobleauth.repository",
      //                "liaison.grobleapi.repository",
      //                "liaison.groblepayment.repository"
    })
public class GrobleAppApplication {
  public static void main(String[] args) {
    SpringApplication.run(GrobleAppApplication.class, args);
  }
}
