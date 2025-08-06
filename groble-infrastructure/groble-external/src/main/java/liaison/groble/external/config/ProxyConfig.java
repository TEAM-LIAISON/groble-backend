package liaison.groble.external.config;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/** HTTP 클라이언트 프록시 설정 프록시 환경에서 외부 API 호출 시 사용됩니다. */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "http.proxy.enabled", havingValue = "true")
public class ProxyConfig {

  @Value("${http.proxy.host:}")
  private String proxyHost;

  @Value("${http.proxy.port:0}")
  private int proxyPort;

  /** 프록시가 설정된 RestTemplate */
  @Bean
  @Primary
  public RestTemplate restTemplate() {
    if (!StringUtils.hasText(proxyHost) || proxyPort <= 0) {
      log.info("프록시 미설정 - 일반 RestTemplate 사용");
      return new RestTemplate();
    }

    log.info("프록시 설정: {}:{}", proxyHost, proxyPort); // 로깅 추가

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    factory.setProxy(proxy);

    factory.setConnectTimeout(10000);
    factory.setReadTimeout(30000);

    return new RestTemplate(factory);
  }

  /** 외부 API 호출용 RestTemplate (별도로 필요한 경우) */
  @Bean("externalApiRestTemplate")
  public RestTemplate externalApiRestTemplate() {
    return restTemplate();
  }
}
