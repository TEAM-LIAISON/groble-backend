package liaison.groble.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "http.proxy.enabled", havingValue = "false", matchIfMissing = true)
public class RestTemplateConfig {

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    log.info("기본 RestTemplate 생성");
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(30000); // 30초
    factory.setReadTimeout(60000); // 60초
    return new RestTemplate(factory);
  }

  @Bean("externalApiRestTemplate")
  public RestTemplate externalApiRestTemplate() {
    return restTemplate();
  }
}
