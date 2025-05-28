package liaison.groble.security.config;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * SecurityContext 설정을 담당하는 Configuration 클래스 ThreadLocal 기반의 SecurityContext 전략을 명시적으로 설정하고, 각 요청마다
 * 독립적인 SecurityContext가 보장되도록 합니다.
 */
@Slf4j
@Configuration
public class SecurityContextConfig {

  /**
   * SecurityContextHolder의 전략을 명시적으로 설정 MODE_THREADLOCAL: 각 스레드마다 독립적인 SecurityContext 보장
   * MODE_INHERITABLETHREADLOCAL: 자식 스레드가 부모의 SecurityContext 상속 MODE_GLOBAL: 모든 스레드가 같은
   * SecurityContext 공유 (절대 사용 금지!)
   */
  @PostConstruct
  public void setSecurityContextHolderStrategy() {
    // ThreadLocal 전략 사용 - 각 스레드별로 독립적인 SecurityContext
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    log.info("SecurityContextHolder 전략 설정 완료: MODE_THREADLOCAL");
  }

  /**
   * SecurityContextRepository를 Bean으로 등록 JWT 기반 인증에서는 실제로 사용되지 않지만, Spring Security의 기본 동작을 명확히 하기
   * 위해 설정
   */
  @Bean
  public SecurityContextRepository securityContextRepository() {
    HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
    // JWT 사용으로 인해 세션에 SecurityContext를 저장하지 않음
    repository.setAllowSessionCreation(false);
    return repository;
  }
}
