package liaison.groble.external.adapter.payment.http;

/**
 * HTTP 클라이언트 어댑터 인터페이스
 *
 * <p>Adapter 패턴을 통해 HTTP 클라이언트 구현체를 추상화합니다. 테스트 가능성과 확장성을 위해 인터페이스로 분리합니다.
 */
public interface HttpClientAdapter {

  /**
   * HTTP POST 요청을 수행합니다.
   *
   * @param request HTTP 요청 정보
   * @return HTTP 응답 결과
   * @throws HttpClientException HTTP 요청 중 오류 발생 시
   */
  HttpResponse post(HttpRequest request) throws HttpClientException;

  /**
   * 연결 타임아웃을 설정합니다.
   *
   * @param timeoutMs 타임아웃 (밀리초)
   */
  void setConnectionTimeout(int timeoutMs);

  /**
   * 읽기 타임아웃을 설정합니다.
   *
   * @param timeoutMs 타임아웃 (밀리초)
   */
  void setReadTimeout(int timeoutMs);
}
