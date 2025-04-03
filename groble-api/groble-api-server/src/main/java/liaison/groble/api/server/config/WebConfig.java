package liaison.groble.api.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 웹 MVC 설정 클래스 CORS, 인터셉터, 리소스 핸들러 등 웹 관련 설정 담당 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /** CORS(Cross-Origin Resource Sharing) 설정 다른 출처(도메인)에서의 API 요청을 허용하기 위한 설정 */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**") // 모든 경로에 대해 CORS 설정 적용
        .allowedOriginPatterns("*") // 모든 출처 허용 (프로덕션에서는 구체적인 도메인 지정 권장)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
        .allowedHeaders("*") // 모든 헤더 허용
        .allowCredentials(true) // 쿠키 포함 요청 허용
        .maxAge(3600); // 3600초 동안 pre-flight 요청 결과를 캐싱
  }
}
