package liaison.groble.external.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * HTTP 클라이언트 프록시 설정
 * 프록시 환경에서 외부 API 호출 시 사용됩니다.
 */
@Configuration
@ConditionalOnProperty(name = "http.proxy.enabled", havingValue = "true")
public class ProxyConfig {
    
    @Value("${http.proxy.host:}")
    private String proxyHost;
    
    @Value("${http.proxy.port:0}")
    private int proxyPort;
    
    /**
     * 프록시가 설정된 RestTemplate
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        if (!StringUtils.hasText(proxyHost) || proxyPort <= 0) {
            return new RestTemplate();
        }
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        factory.setProxy(proxy);
        
        // 타임아웃 설정
        factory.setConnectTimeout(10000); // 10초
        factory.setReadTimeout(30000);    // 30초
        
        return new RestTemplate(factory);
    }
    
    /**
     * 외부 API 호출용 RestTemplate (별도로 필요한 경우)
     */
    @Bean("externalApiRestTemplate")
    public RestTemplate externalApiRestTemplate() {
        return restTemplate();
    }

}
