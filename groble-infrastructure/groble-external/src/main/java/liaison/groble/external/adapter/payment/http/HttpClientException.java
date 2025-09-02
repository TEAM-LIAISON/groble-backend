package liaison.groble.external.adapter.payment.http;

/**
 * HTTP 클라이언트 예외
 *
 * <p>HTTP 요청 중 발생하는 모든 예외를 캡슐화합니다.
 */
public class HttpClientException extends Exception {

  private final String url;
  private final int statusCode;
  private final long responseTimeMs;

  public HttpClientException(String message, String url) {
    super(message);
    this.url = url;
    this.statusCode = -1;
    this.responseTimeMs = -1;
  }

  public HttpClientException(String message, String url, Throwable cause) {
    super(message, cause);
    this.url = url;
    this.statusCode = -1;
    this.responseTimeMs = -1;
  }

  public HttpClientException(String message, String url, int statusCode, long responseTimeMs) {
    super(message);
    this.url = url;
    this.statusCode = statusCode;
    this.responseTimeMs = responseTimeMs;
  }

  public HttpClientException(
      String message, String url, int statusCode, long responseTimeMs, Throwable cause) {
    super(message, cause);
    this.url = url;
    this.statusCode = statusCode;
    this.responseTimeMs = responseTimeMs;
  }

  public String getUrl() {
    return url;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public long getResponseTimeMs() {
    return responseTimeMs;
  }

  @Override
  public String toString() {
    return String.format(
        "HttpClientException{message='%s', url='%s', statusCode=%d, responseTimeMs=%d}",
        getMessage(), url, statusCode, responseTimeMs);
  }
}
