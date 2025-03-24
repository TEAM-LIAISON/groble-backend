package liaison.grobleauth.service;

import org.springframework.stereotype.Service;

import liaison.grobleauth.model.TokenType;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 인증 관련 메트릭을 수집하는 서비스 Prometheus/Micrometer를 활용한 모니터링 지원 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
  private final MeterRegistry meterRegistry;

  private static final String PREFIX = "auth_service_";

  /** 토큰 생성 이벤트 기록 */
  public void recordTokenGeneration(TokenType tokenType) {
    try {
      String metricName = PREFIX + "token_generation_total";
      Counter counter =
          Counter.builder(metricName)
              .tag("token_type", tokenType.name().toLowerCase())
              .description("토큰 생성 횟수")
              .register(meterRegistry);
      counter.increment();
    } catch (Exception e) {
      log.warn("메트릭 기록 실패: {}", e.getMessage());
    }
  }
}
