package liaison.groble.security.jwt;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 인증 관련 메트릭을 수집하는 서비스 Prometheus/Micrometer를 활용한 모니터링 지원 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
  //  private final MeterRegistry meterRegistry;
  //
  //  private static final String PREFIX = "auth_service_";
  //
  //  /** 토큰 생성 이벤트 기록 */
  //  public void recordTokenGeneration(TokenType tokenType) {
  //    try {
  //      String metricName = PREFIX + "token_generation_total";
  //      Counter counter =
  //          Counter.builder(metricName)
  //              .tag("token_type", tokenType.name().toLowerCase())
  //              .description("토큰 생성 횟수")
  //              .register(meterRegistry);
  //      counter.increment();
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
  //
  //  /** 토큰 검증 성공 이벤트 기록 */
  //  public void recordTokenValidationSuccess(TokenType tokenType) {
  //    try {
  //      String metricName = PREFIX + "token_validation_total";
  //      Counter counter =
  //          Counter.builder(metricName)
  //              .tag("token_type", tokenType.name().toLowerCase())
  //              .tag("result", "success")
  //              .description("토큰 검증 결과")
  //              .register(meterRegistry);
  //      counter.increment();
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
  //
  //  /** 토큰 검증 실패 이벤트 기록 */
  //  public void recordTokenValidationFailure(TokenType tokenType, String reason) {
  //    try {
  //      String metricName = PREFIX + "token_validation_total";
  //      Counter counter =
  //          Counter.builder(metricName)
  //              .tag("token_type", tokenType.name().toLowerCase())
  //              .tag("result", "failure")
  //              .tag("reason", reason)
  //              .description("토큰 검증 결과")
  //              .register(meterRegistry);
  //      counter.increment();
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
  //
  //  /** 인증 시도 이벤트 기록 */
  //  public void recordAuthenticationAttempt(String provider, boolean success) {
  //    try {
  //      String metricName = PREFIX + "authentication_attempts_total";
  //      Counter counter =
  //          Counter.builder(metricName)
  //              .tag("provider", provider)
  //              .tag("result", success ? "success" : "failure")
  //              .description("인증 시도 횟수")
  //              .register(meterRegistry);
  //      counter.increment();
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
  //
  //  /**
  //   * 인증 처리 시간 기록 시작
  //   *
  //   * @return 타이머 객체
  //   */
  //  public Timer.Sample startAuthenticationTimer() {
  //    return Timer.start(meterRegistry);
  //  }
  //
  //  /**
  //   * 인증 처리 시간 기록 완료
  //   *
  //   * @param sample 시작 시 생성된 타이머 샘플
  //   * @param provider 인증 제공자
  //   * @param success 인증 성공 여부
  //   */
  //  public void stopAuthenticationTimer(Timer.Sample sample, String provider, boolean success) {
  //    try {
  //      String metricName = PREFIX + "authentication_duration_seconds";
  //      Timer timer =
  //          Timer.builder(metricName)
  //              .tag("provider", provider)
  //              .tag("result", success ? "success" : "failure")
  //              .description("인증 처리 시간 (초)")
  //              .register(meterRegistry);
  //      sample.stop(timer);
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
  //
  //  /** 로그인 이벤트 기록 */
  //  public void recordLoginEvent(String provider, boolean success) {
  //    try {
  //      String metricName = PREFIX + "login_total";
  //      Counter counter =
  //          Counter.builder(metricName)
  //              .tag("provider", provider)
  //              .tag("result", success ? "success" : "failure")
  //              .description("로그인 횟수")
  //              .register(meterRegistry);
  //      counter.increment();
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
  //
  //  /** 로그아웃 이벤트 기록 */
  //  public void recordLogoutEvent() {
  //    try {
  //      String metricName = PREFIX + "logout_total";
  //      Counter counter = Counter.builder(metricName).description("로그아웃
  // 횟수").register(meterRegistry);
  //      counter.increment();
  //    } catch (Exception e) {
  //      log.warn("메트릭 기록 실패: {}", e.getMessage());
  //    }
  //  }
}
