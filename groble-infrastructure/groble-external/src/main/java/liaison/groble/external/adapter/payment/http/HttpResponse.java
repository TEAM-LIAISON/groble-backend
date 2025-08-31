package liaison.groble.external.adapter.payment.http;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * HTTP 응답 정보를 캡슐화하는 Value Object
 *
 * <p>HTTP 응답의 모든 정보를 불변 객체로 관리합니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class HttpResponse {

  private final int statusCode;
  private final String body;
  private final Map<String, String> headers;
  private final long responseTimeMs;

  /**
   * 성공 응답인지 확인합니다.
   *
   * @return HTTP 상태 코드가 200번대인 경우 true
   */
  public boolean isSuccess() {
    return statusCode >= 200 && statusCode < 300;
  }

  /**
   * 클라이언트 오류인지 확인합니다.
   *
   * @return HTTP 상태 코드가 400번대인 경우 true
   */
  public boolean isClientError() {
    return statusCode >= 400 && statusCode < 500;
  }

  /**
   * 서버 오류인지 확인합니다.
   *
   * @return HTTP 상태 코드가 500번대인 경우 true
   */
  public boolean isServerError() {
    return statusCode >= 500 && statusCode < 600;
  }
}
