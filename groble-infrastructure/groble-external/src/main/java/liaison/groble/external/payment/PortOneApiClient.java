package liaison.groble.external.payment;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
  private final PortOneProperties portOneProperties;

  // API 토큰과 만료 시간 캐싱
  private String accessToken;
  private Instant tokenExpiry = Instant.EPOCH;

  /**
   * 포트원 V2 API 호출
   *
   * @param path API 경로
   * @param method HTTP 메서드
   * @param requestBody 요청 본문 (null 가능)
   * @param responseType 응답 타입
   * @return API 응답
   * @param <T> 응답 타입
   */
  public <T> T callApi(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
    // API 기본 URL
    String apiUrl = portOneProperties.getApiUrlV2() + path;

    // 요청 헤더 구성
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 요청 ID 생성
    String requestId = UUID.randomUUID().toString();
    headers.set("X-Request-Id", requestId);

    // 인증 토큰 가져오기
    String token = getAccessToken();
    headers.set("Authorization", "Bearer " + token);

    // HTTP 엔티티 생성
    HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

    log.debug("Calling PortOne API: {} {}", method, apiUrl);
    ResponseEntity<T> response = restTemplate.exchange(apiUrl, method, entity, responseType);

    return response.getBody();
  }

  /**
   * 포트원 API 접근 토큰 가져오기 캐싱된 토큰이 있으면 재사용, 없으면 새로 발급
   *
   * @return 액세스 토큰
   */
  private String getAccessToken() {
    // 토큰이 유효하면 재사용
    if (accessToken != null && Instant.now().isBefore(tokenExpiry)) {
      log.debug("Using cached PortOne API token");
      return accessToken;
    }

    log.debug("Requesting new PortOne API token");

    // 토큰 갱신 API 호출
    String tokenUrl = portOneProperties.getApiUrlV2() + "/login/api-secret";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    log.info("PortOne API token URL: {}", tokenUrl);

    Map<String, String> requestBody = Map.of("apiSecret", portOneProperties.getApiSecret());

    log.info("PortOne API token request body: {}", requestBody);

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<Map> response =
          restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

      Map<String, Object> responseBody = response.getBody();

      if (responseBody != null && responseBody.containsKey("accessToken")) {
        accessToken = (String) responseBody.get("accessToken");

        // 만료 시간 설정 (응답에서 가져오거나 기본값 사용)
        Integer expiresIn = (Integer) responseBody.getOrDefault("expires_in", 3600); // 기본 1시간
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 60); // 조금 일찍 만료되도록 설정

        log.debug("Successfully obtained new PortOne API token");
        return accessToken;
      } else {
        log.error("Failed to get access token: {}", responseBody);
        throw new RuntimeException("포트원 API 토큰 획득 실패");
      }
    } catch (Exception e) {
      log.error("Error getting access token", e);
      throw new RuntimeException("포트원 API 토큰 획득 중 오류 발생", e);
    }
  }

  /**
   * 웹훅 서명 검증 (V2 API)
   *
   * @param signature 서명 값
   * @param payload 웹훅 본문
   * @return 검증 결과
   */
  public boolean verifyWebhookSignature(String signature, String payload) {
    // 여기에 V2 API 웹훅 서명 검증 로직 구현
    // 실제 구현은 포트원 V2 문서 참조
    return true; // 임시 구현
  }
}
