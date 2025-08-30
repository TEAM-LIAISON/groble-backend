package liaison.groble.external.adapter.payment.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

/**
 * 기본 HTTP 클라이언트 어댑터 구현체
 *
 * <p>HttpURLConnection을 사용한 HTTP 클라이언트 구현체입니다. Template Method 패턴을 통해 공통 처리 로직을 추상화하고, 리소스 관리와 예외
 * 처리를 체계적으로 수행합니다.
 */
@Slf4j
@Component
@Primary
public class DefaultHttpClientAdapter implements HttpClientAdapter {

  private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
  private static final String USER_AGENT = "Groble-HttpClient/1.0";

  private int defaultConnectionTimeout = 5000; // 5초
  private int defaultReadTimeout = 10000; // 10초

  @Override
  public HttpResponse post(HttpRequest request) throws HttpClientException {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    HttpURLConnection connection = null;
    try {
      log.debug("HTTP POST 요청 시작 - URL: {}", request.getUrl());

      connection = createConnection(request);
      setupConnection(connection, request);
      sendRequest(connection, request);

      HttpResponse response = receiveResponse(connection, stopWatch);

      log.debug(
          "HTTP POST 요청 완료 - URL: {}, 응답시간: {}ms, 상태코드: {}",
          request.getUrl(),
          response.getResponseTimeMs(),
          response.getStatusCode());

      return response;

    } catch (SocketTimeoutException e) {
      stopWatch.stop();
      log.error(
          "HTTP 요청 타임아웃 - URL: {}, 소요시간: {}ms", request.getUrl(), stopWatch.getTotalTimeMillis());
      throw new HttpClientException(
          "HTTP 요청 타임아웃", request.getUrl(), -1, (int) stopWatch.getTotalTimeMillis(), e);
    } catch (IOException e) {
      stopWatch.stop();
      log.error(
          "HTTP 요청 I/O 오류 - URL: {}, 소요시간: {}ms",
          request.getUrl(),
          stopWatch.getTotalTimeMillis(),
          e);
      throw new HttpClientException(
          "HTTP 요청 I/O 오류: " + e.getMessage(),
          request.getUrl(),
          -1,
          (int) stopWatch.getTotalTimeMillis(),
          e);
    } catch (Exception e) {
      stopWatch.stop();
      log.error(
          "HTTP 요청 예상치 못한 오류 - URL: {}, 소요시간: {}ms",
          request.getUrl(),
          stopWatch.getTotalTimeMillis(),
          e);
      throw new HttpClientException(
          "HTTP 요청 예상치 못한 오류: " + e.getMessage(),
          request.getUrl(),
          -1,
          (int) stopWatch.getTotalTimeMillis(),
          e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @Override
  public void setConnectionTimeout(int timeoutMs) {
    this.defaultConnectionTimeout = timeoutMs;
  }

  @Override
  public void setReadTimeout(int timeoutMs) {
    this.defaultReadTimeout = timeoutMs;
  }

  private HttpURLConnection createConnection(HttpRequest request) throws IOException {
    URL url = new URL(request.getUrl());
    return (HttpURLConnection) url.openConnection();
  }

  private void setupConnection(HttpURLConnection connection, HttpRequest request)
      throws IOException {
    // 기본 설정
    connection.setRequestMethod(request.getMethod());
    connection.setDoOutput(true);
    connection.setDoInput(true);

    // 타임아웃 설정
    int connectionTimeout =
        request.getConnectionTimeoutMs() > 0
            ? request.getConnectionTimeoutMs()
            : defaultConnectionTimeout;
    int readTimeout =
        request.getReadTimeoutMs() > 0 ? request.getReadTimeoutMs() : defaultReadTimeout;

    connection.setConnectTimeout(connectionTimeout);
    connection.setReadTimeout(readTimeout);

    // 기본 헤더 설정
    connection.setRequestProperty("Content-Type", CONTENT_TYPE_JSON);
    connection.setRequestProperty("User-Agent", USER_AGENT);
    connection.setRequestProperty("Accept", "application/json");

    // 커스텀 헤더 설정
    if (request.getHeaders() != null) {
      request.getHeaders().forEach(connection::setRequestProperty);
    }
  }

  private void sendRequest(HttpURLConnection connection, HttpRequest request) throws IOException {
    if (request.getBody() != null && !request.getBody().isEmpty()) {
      try (OutputStreamWriter writer =
          new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
        writer.write(request.getBody());
        writer.flush();
      }
    }
  }

  private HttpResponse receiveResponse(HttpURLConnection connection, StopWatch stopWatch)
      throws IOException {
    int statusCode = connection.getResponseCode();

    // 응답 본문 읽기
    String responseBody;
    if (statusCode >= 200 && statusCode < 300) {
      responseBody = readInputStream(connection.getInputStream());
    } else {
      responseBody = readInputStream(connection.getErrorStream());
    }

    // 응답 헤더 읽기
    Map<String, String> responseHeaders = new HashMap<>();
    connection
        .getHeaderFields()
        .forEach(
            (key, values) -> {
              if (key != null && !values.isEmpty()) {
                responseHeaders.put(key, values.get(0));
              }
            });

    stopWatch.stop();

    return HttpResponse.builder()
        .statusCode(statusCode)
        .body(responseBody)
        .headers(responseHeaders)
        .responseTimeMs(stopWatch.getTotalTimeMillis())
        .build();
  }

  private String readInputStream(java.io.InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return "";
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line).append("\n");
      }
    }
    return response.toString().trim();
  }
}
