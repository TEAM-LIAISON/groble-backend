package liaison.groble.external.adapter.payment.http;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * HTTP 요청 정보를 캡슐화하는 Value Object
 *
 * <p>HTTP 요청에 필요한 모든 정보를 불변 객체로 관리합니다. Builder 패턴을 통해 선택적 파라미터를 지원합니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class HttpRequest {

  private final String url;
  private final String method;
  private final Map<String, String> headers;
  private final String body;
  private final int connectionTimeoutMs;
  private final int readTimeoutMs;

  /**
   * POST 요청을 생성합니다.
   *
   * @param url 요청 URL
   * @param body 요청 본문
   * @return POST 요청 객체
   */
  public static HttpRequest post(String url, String body) {
    return HttpRequest.builder()
        .url(url)
        .method("POST")
        .body(body)
        .connectionTimeoutMs(5000) // 기본값 5초
        .readTimeoutMs(10000) // 기본값 10초
        .build();
  }

  /**
   * POST 요청을 생성합니다 (헤더 포함).
   *
   * @param url 요청 URL
   * @param headers 헤더 맵
   * @param body 요청 본문
   * @return POST 요청 객체
   */
  public static HttpRequest postWithHeaders(String url, Map<String, String> headers, String body) {
    return HttpRequest.builder()
        .url(url)
        .method("POST")
        .headers(headers)
        .body(body)
        .connectionTimeoutMs(5000)
        .readTimeoutMs(10000)
        .build();
  }
}
