package liaison.groble.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PortOneConfig {

  @Bean
  public RestTemplate portOneRestTemplate() {
    return new RestTemplate();
  }
}
