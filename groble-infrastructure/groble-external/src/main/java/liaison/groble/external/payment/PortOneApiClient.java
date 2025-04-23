package liaison.groble.external.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import liaison.groble.external.config.PortOneProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneApiClient {

  private final RestTemplate restTemplate;
  private final PortOneProperties properties;

  /**
   * 포트원 API 호출 메서드
   *
   * @param path API 경로
   * @param method HTTP 메서드
   * @param requestData 요청 데이터 (POST 요청일 경우)
   * @param responseType 응답 타입 클래스
   * @return API 응답
   */
  public <T> T callApi(String path, HttpMethod method, Object requestData, Class<T> responseType) {
    String url = properties.getBaseUrl() + path;

    // 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", getAuthorizationHeader());

    // 요청 엔티티 생성
    HttpEntity<?> entity;
    if (requestData != null) {
      entity = new HttpEntity<>(requestData, headers);
    } else {
      entity = new HttpEntity<>(headers);
    }

    // API 호출
    log.debug("Calling PortOne API: {} {}", method, url);

    ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

    log.debug("PortOne API response status: {}", response.getStatusCode());
    return response.getBody();
  }

  /**
   * 인증 헤더 생성
   *
   * @return 인증 헤더 값
   */
  private String getAuthorizationHeader() {
    // 포트원 API 키를 Base64로 인코딩
    String auth = properties.getApiKey() + ":";
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encodedAuth;
  }

  /**
   * 웹훅 서명 검증
   *
   * @param webhookData 웹훅 데이터
   * @param signature 서명
   * @return 검증 결과
   */
  public boolean verifyWebhookSignature(String payload, String signature) {
    // TODO: 포트원 웹훅 서명 검증 로직 구현
    // 실제 구현에서는 HMAC-SHA256 서명 검증 로직 필요
    return true;
  }
}
