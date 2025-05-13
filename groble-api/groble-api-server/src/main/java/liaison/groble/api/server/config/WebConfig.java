package liaison.groble.api.server.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import liaison.groble.security.interceptor.RoleCheckInterceptor;
import liaison.groble.security.resolver.AuthArgumentResolver;

import lombok.RequiredArgsConstructor;

/** 웹 MVC 설정 클래스 CORS, 인터셉터, 리소스 핸들러 등 웹 관련 설정 담당 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AuthArgumentResolver authArgumentResolver;
  private final RoleCheckInterceptor roleCheckInterceptor;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authArgumentResolver);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(roleCheckInterceptor).addPathPatterns("/api/v1/**");
  }

  // 3. WebConfig.java CORS 설정 업데이트
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**") // 모든 경로에 대해 CORS 설정 적용
        .allowedOrigins(
            "http://localhost:3000",
            "https://dev.groble.im",
            "https://api.dev.groble.im",
            "https://groble.im",
            "https://api.groble.im")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
        .allowedHeaders("*") // 모든 헤더 허용
        .allowCredentials(true) // 쿠키 포함 요청 허용
        .maxAge(3600) // 3600초 동안 pre-flight 요청 결과를 캐싱
        .exposedHeaders(HttpHeaders.LOCATION, HttpHeaders.SET_COOKIE); // 노출할 헤더 설정
  }
}
