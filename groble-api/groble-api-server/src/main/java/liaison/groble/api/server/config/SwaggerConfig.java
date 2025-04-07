package liaison.groble.api.server.config;

import java.util.Arrays;
import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/** Swagger(OpenAPI) 설정 클래스 API 문서화를 위한 Swagger UI 및 OpenAPI 스펙 구성 */
@Configuration
public class SwaggerConfig {

  @Value("${app.backend-url:http://localhost:8080}")
  private String backendUrl;

  @Bean
  public OpenAPI openAPI() {
    // API 정보 설정
    Info info =
        new Info()
            .title("Groble API")
            .description("Groble 서비스의 API 명세서입니다.")
            .version("v1.0.0")
            .contact(new Contact().name("Groble Team").email("contact@groble.com"))
            .license(
                new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

    // 서버 정보 설정
    Server localServer = new Server().url(backendUrl).description("Local Server");

    // JWT 인증 스키마 설정
    SecurityScheme jwtSecurityScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    // API 문서에 인증 정보 추가
    SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

    return new OpenAPI()
        .info(info)
        .servers(List.of(localServer))
        .components(new Components().addSecuritySchemes("bearerAuth", jwtSecurityScheme))
        .security(Arrays.asList(securityRequirement));
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
