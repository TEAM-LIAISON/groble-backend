package liaison.groble.api.server.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/** 간소화된 OpenAPI 설정 - 복잡한 커스텀 어노테이션 대신 OpenAPI Generator 활용 - 실제 문서는 YAML 파일로 관리 */
@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Groble API")
                .version("v1.0.0")
                .description("Groble 서비스 통합 API 문서")
                .contact(new Contact().name("Groble Team").email("dev@groble.com")))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("로컬 서버"),
                new Server().url("https://api.groble.com").description("운영 서버")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .security(List.of(new SecurityRequirement().addList("bearerAuth")));
  }

  @Bean
  public ForwardedHeaderFilter forwardedHeaderFilter() {
    return new ForwardedHeaderFilter();
  }

  // API 그룹핑은 태그 기반으로 자동 처리
  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder().group("public").pathsToMatch("/api/v1/**").build();
  }
}
