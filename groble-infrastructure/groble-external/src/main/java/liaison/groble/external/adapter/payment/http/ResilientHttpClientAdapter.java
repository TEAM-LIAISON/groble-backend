package liaison.groble.external.adapter.payment.http;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 탄력성 있는 HTTP 클라이언트 어댑터
 *
 * <p>Circuit Breaker 패턴과 Retry 패턴을 조합하여 외부 API 호출의 안정성을 확보합니다. Decorator 패턴을 통해 기본 HTTP 클라이언트에 탄력성
 * 기능을 추가합니다.
 *
 * <p><strong>적용된 패턴:</strong>
 *
 * <ul>
 *   <li>Circuit Breaker Pattern: 연속 실패 시 차단
 *   <li>Retry Pattern: 지수 백오프로 재시도
 *   <li>Decorator Pattern: 기본 클라이언트 기능 확장
 * </ul>
 */
@Slf4j
@Component
public class ResilientHttpClientAdapter implements HttpClientAdapter {

  private final HttpClientAdapter delegate;

  // Circuit Breaker 상태
  private enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
  }

  private volatile CircuitState circuitState = CircuitState.CLOSED;

  // Circuit Breaker 설정
  private static final int FAILURE_THRESHOLD = 5; // 실패 임계값
  private static final int TIMEOUT_DURATION_SECONDS = 60; // 차단 시간 (초)
  private static final int SUCCESS_THRESHOLD = 2; // 복구 임계값

  // 통계
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private final AtomicInteger halfOpenSuccesses = new AtomicInteger(0);
  private final AtomicLong lastFailureTime = new AtomicLong(0);

  public ResilientHttpClientAdapter(DefaultHttpClientAdapter delegate) {
    this.delegate = delegate;
  }

  @Override
  @Retryable(
      value = {HttpClientException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000))
  public HttpResponse post(HttpRequest request) throws HttpClientException {
    // Circuit Breaker 상태 확인
    if (isCircuitOpen()) {
      log.warn("Circuit Breaker OPEN 상태 - 요청 차단: {}", request.getUrl());
      throw new HttpClientException("Circuit Breaker OPEN - 서비스 일시적으로 사용 불가", request.getUrl());
    }

    try {
      log.debug("HTTP 요청 실행 (Circuit: {}) - URL: {}", circuitState, request.getUrl());

      HttpResponse response = delegate.post(request);

      // 성공 시 Circuit Breaker 상태 업데이트
      handleSuccess();

      return response;

    } catch (HttpClientException e) {
      // 실패 시 Circuit Breaker 상태 업데이트
      handleFailure();

      log.error(
          "HTTP 요청 실패 (연속실패: {}, Circuit: {}) - URL: {}, 오류: {}",
          consecutiveFailures.get(),
          circuitState,
          request.getUrl(),
          e.getMessage());

      throw e;
    }
  }

  @Override
  public void setConnectionTimeout(int timeoutMs) {
    delegate.setConnectionTimeout(timeoutMs);
  }

  @Override
  public void setReadTimeout(int timeoutMs) {
    delegate.setReadTimeout(timeoutMs);
  }

  private boolean isCircuitOpen() {
    if (circuitState == CircuitState.OPEN) {
      // 타임아웃 시간이 지났으면 HALF_OPEN으로 전환
      if (isTimeoutExpired()) {
        circuitState = CircuitState.HALF_OPEN;
        halfOpenSuccesses.set(0);
        log.info("Circuit Breaker HALF_OPEN 전환 - 복구 시도 시작");
        return false;
      }
      return true;
    }
    return false;
  }

  private boolean isTimeoutExpired() {
    long lastFailure = lastFailureTime.get();
    if (lastFailure == 0) return false;

    LocalDateTime failureTime =
        LocalDateTime.ofEpochSecond(lastFailure / 1000, 0, java.time.ZoneOffset.UTC);
    LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

    return ChronoUnit.SECONDS.between(failureTime, now) >= TIMEOUT_DURATION_SECONDS;
  }

  private void handleSuccess() {
    if (circuitState == CircuitState.HALF_OPEN) {
      int successes = halfOpenSuccesses.incrementAndGet();
      if (successes >= SUCCESS_THRESHOLD) {
        // Circuit 완전 복구
        circuitState = CircuitState.CLOSED;
        consecutiveFailures.set(0);
        lastFailureTime.set(0);
        log.info("Circuit Breaker CLOSED 전환 - 서비스 완전 복구");
      }
    } else if (circuitState == CircuitState.CLOSED) {
      // 성공 시 실패 카운터 리셋
      consecutiveFailures.set(0);
    }
  }

  private void handleFailure() {
    int failures = consecutiveFailures.incrementAndGet();
    lastFailureTime.set(System.currentTimeMillis());

    if (circuitState == CircuitState.CLOSED && failures >= FAILURE_THRESHOLD) {
      // Circuit OPEN으로 전환
      circuitState = CircuitState.OPEN;
      log.error(
          "Circuit Breaker OPEN 전환 - 연속 실패 {}회, {}초 후 복구 시도", failures, TIMEOUT_DURATION_SECONDS);
    } else if (circuitState == CircuitState.HALF_OPEN) {
      // HALF_OPEN에서 실패하면 다시 OPEN
      circuitState = CircuitState.OPEN;
      log.warn("Circuit Breaker OPEN 재전환 - 복구 시도 실패");
    }
  }

  /**
   * Circuit Breaker 상태 정보를 반환합니다. (모니터링 용도)
   *
   * @return 상태 정보 문자열
   */
  public String getCircuitStatus() {
    return String.format(
        "Circuit: %s, 연속실패: %d, 마지막실패: %s",
        circuitState,
        consecutiveFailures.get(),
        lastFailureTime.get() > 0
            ? LocalDateTime.ofEpochSecond(lastFailureTime.get() / 1000, 0, java.time.ZoneOffset.UTC)
            : "없음");
  }
}
