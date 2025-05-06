package liaison.groble.api.server.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Swagger(OpenAPI) 설정 클래스 API 문서화를 위한 Swagger UI 및 OpenAPI 스펙 구성 */
@Configuration
public class SwaggerConfig {

  @Value("${app.backend-url:http://localhost:8080}")
  private String backendUrl;

  @Bean
  public OpenAPI openAPI() {
    // 기본 설정
    Info info =
        new Info().title("Groble API").description("Groble 서비스의 API 명세서입니다.").version("v1.0.0");

    // JWT 설정
    SecurityScheme jwtSecurityScheme =
        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");

    // 컴포넌트 설정
    Components components = new Components().addSecuritySchemes("bearerAuth", jwtSecurityScheme);

    return new OpenAPI().info(info).components(components);
  }

  @Bean
  public OpenApiCustomizer enumCustomizer() {
    return openApi -> {
      // ResponseStatus enum 문서화 개선
      Schema responseStatusSchema = new Schema().type("string").description("응답 상태 타입");

      // enum 값 추가
      responseStatusSchema.setEnum(List.of("SUCCESS", "ERROR", "FAIL"));

      openApi.getComponents().addSchemas("ResponseStatus", responseStatusSchema);
    };
  }

  @Bean
  public GroupedOpenApi authApi() {
    return GroupedOpenApi.builder()
        .group("인증 API")
        .pathsToMatch("/api/v1/auth/**")
        .packagesToScan("liaison.groble.api.server.auth")
        .build();
  }

  @Bean
  public GroupedOpenApi userApi() {
    return GroupedOpenApi.builder()
        .group("사용자 API")
        .pathsToMatch("/api/v1/users/**")
        .packagesToScan("liaison.groble.api.server.user")
        .build();
  }

  @Bean
  public GroupedOpenApi allApi() {
    return GroupedOpenApi.builder()
        .group("모든 API")
        .pathsToMatch("/api/**")
        .packagesToScan("liaison.groble.api.server")
        .build();
  }
}
