package liaison.grobleapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
    basePackages = {
      "liaison.grobleauth",
      "liaison.grobleapi",
      "liaison.groblecommon",
      "liaison.groblecore",
      "liaison.groblepayment",
    })
public class GrobleAppApplication {
  public static void main(String[] args) {
    SpringApplication.run(GrobleAppApplication.class, args);
  }
}
