package liaison.groble.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ncp.sms")
public class SmsConfig {
  private String accessKey;
  private String secretKey;
  private String serviceId;
  private String sendFrom;

  @Bean
  public RestTemplate portOneRestTemplate() {
    return new RestTemplate();
  }
}
