package liaison.groble.external.infotalk.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import liaison.groble.external.config.BizppurioConfig;
import liaison.groble.external.infotalk.dto.TokenResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 비즈뿌리오 인증토큰 관리 서비스
 *
 * <p>이 클래스는 다음과 같은 핵심 기능을 제공합니다: 1. 토큰 발급 및 갱신 2. 토큰 유효성 검증 3. 자동 갱신을 통한 토큰 관리
 */
@Slf4j
@Service
public class BizppurioTokenService {
  private final BizppurioConfig config;
  private final RestTemplate restTemplate;

  // 토큰 정보를 메모리에 캐싱
  // volatile 키워드로 멀티스레드 환경에서의 가시성 보장
  private volatile String currentToken;
  private volatile LocalDateTime tokenExpireTime;

  // 날짜 포맷터 (스레드 세이프하게 static final로 선언)
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  // 토큰 발급 API 엔드포인트
  private static final String TOKEN_ENDPOINT = "/v1/token";

  public BizppurioTokenService(BizppurioConfig config, RestTemplateBuilder restTemplateBuilder) {
    this.config = config;

    // RestTemplate 설정: 타임아웃 설정을 적용
    this.restTemplate =
        restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(config.getConnectTimeout()))
            .setReadTimeout(Duration.ofMillis(config.getReadTimeout()))
            .build();
  }

  /** 애플리케이션 시작 시 토큰을 미리 발급받습니다. 이렇게 하면 첫 API 호출 시 지연을 방지할 수 있습니다. */
  @PostConstruct
  public void initializeToken() {
    try {
      log.info("비즈뿌리오 초기 토큰 발급 시작");
      issueNewToken();
    } catch (Exception e) {
      // 초기화 실패는 경고만 하고 진행 (첫 API 호출 시 재시도)
      log.warn("초기 토큰 발급 실패. 첫 API 호출 시 재시도합니다.", e);
    }
  }

  /**
   * 유효한 토큰을 반환합니다.
   *
   * <p>synchronized 키워드로 동시성 제어: - 여러 스레드가 동시에 토큰을 요청해도 중복 발급을 방지 - 토큰 갱신 중 다른 요청은 대기 후 갱신된 토큰을 받음
   *
   * @return 유효한 Bearer 토큰
   */
  public synchronized String getValidToken() {
    // 토큰이 유효한지 검증
    if (!isTokenValid()) {
      log.info("토큰이 만료되었거나 없습니다. 새로 발급받습니다.");
      issueNewToken();
    }

    return currentToken;
  }

  /**
   * 토큰 유효성을 검증합니다.
   *
   * <p>검증 로직: 1. 토큰이 존재하는지 확인 2. 만료 시간이 설정되어 있는지 확인 3. 현재 시간이 (만료시간 - 여유시간) 이전인지 확인
   *
   * @return 토큰이 유효하면 true
   */
  private boolean isTokenValid() {
    // 토큰이 없거나 만료시간이 없으면 무효
    if (currentToken == null || tokenExpireTime == null) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();

    // 여유시간을 두고 미리 갱신 (예: 만료 1시간 전)
    // 이렇게 하면 정확히 만료 시점에 API 호출이 실패하는 것을 방지
    LocalDateTime refreshThreshold =
        tokenExpireTime.minusMinutes(config.getTokenRefreshMarginMinutes());

    boolean isValid = now.isBefore(refreshThreshold);

    if (!isValid) {
      log.debug("토큰 갱신 필요 - 현재: {}, 갱신기준: {}, 만료: {}", now, refreshThreshold, tokenExpireTime);
    }

    return isValid;
  }

  /**
   * 새로운 토큰을 발급받습니다.
   *
   * <p>구현 포인트: 1. Basic Authentication 헤더 생성 2. POST 요청으로 토큰 발급 3. 응답에서 토큰과 만료시간 추출 4. 메모리에 캐싱
   */
  private void issueNewToken() {
    try {
      // API URL 구성
      String url = config.getApiUrl() + TOKEN_ENDPOINT;

      // HTTP 헤더 설정
      HttpHeaders headers = createTokenRequestHeaders();

      // 요청 엔티티 생성 (body는 비어있음)
      HttpEntity<Void> request = new HttpEntity<>(headers);

      log.debug("토큰 발급 요청 - URL: {}", url);

      // POST 요청 실행
      ResponseEntity<TokenResponse> response =
          restTemplate.exchange(url, HttpMethod.POST, request, TokenResponse.class);

      // 응답 처리
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        handleTokenResponse(response.getBody());
      } else {
        throw new RuntimeException("토큰 발급 실패 - 응답코드: " + response.getStatusCode());
      }

    } catch (HttpClientErrorException e) {
      // HTTP 4xx 에러 처리 (인증 실패 등)
      log.error("토큰 발급 실패 - HTTP 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new RuntimeException("토큰 발급 실패: 인증 정보를 확인하세요", e);

    } catch (Exception e) {
      // 기타 에러 처리 (네트워크 오류 등)
      log.error("토큰 발급 중 예외 발생", e);
      throw new RuntimeException("토큰 발급 실패", e);
    }
  }

  /**
   * 토큰 요청용 HTTP 헤더를 생성합니다.
   *
   * <p>Basic Authentication 구현: 1. "계정:비밀번호" 형식의 문자열 생성 2. UTF-8로 인코딩 3. Base64로 인코딩 4. "Basic "
   * 접두어 추가 (공백 주의!)
   */
  private HttpHeaders createTokenRequestHeaders() {
    HttpHeaders headers = new HttpHeaders();

    // Content-Type 설정
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("charset", "utf-8");

    // Basic Authentication 헤더 생성
    String credentials = config.getAccount() + ":" + config.getPassword();
    byte[] credentialsBytes = credentials.getBytes(StandardCharsets.UTF_8);
    String encodedCredentials = Base64.getEncoder().encodeToString(credentialsBytes);

    // "Basic " 다음에 공백이 정확히 하나 있어야 함!
    headers.set("Authorization", "Basic " + encodedCredentials);

    return headers;
  }

  /** 토큰 응답을 처리하고 메모리에 저장합니다. */
  private void handleTokenResponse(TokenResponse tokenResponse) {
    // 토큰 저장
    this.currentToken = tokenResponse.getAccessToken();

    // 만료시간 파싱 및 저장
    this.tokenExpireTime = LocalDateTime.parse(tokenResponse.getExpired(), DATE_FORMATTER);

    log.info("토큰 발급 성공 - 타입: {}, 만료시간: {}", tokenResponse.getType(), tokenExpireTime);

    // 토큰 길이 로깅 (보안상 토큰 자체는 로깅하지 않음)
    log.debug("토큰 길이: {} 문자", currentToken.length());
  }

  /** 현재 토큰의 남은 유효시간을 분 단위로 반환합니다. 모니터링이나 디버깅에 유용합니다. */
  public long getRemainingTokenMinutes() {
    if (tokenExpireTime == null) {
      return 0;
    }

    Duration remaining = Duration.between(LocalDateTime.now(), tokenExpireTime);
    return remaining.toMinutes();
  }
}
