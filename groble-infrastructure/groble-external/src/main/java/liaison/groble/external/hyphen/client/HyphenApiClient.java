package liaison.groble.external.hyphen.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import liaison.groble.external.hyphen.config.HyphenConfig;
import liaison.groble.external.hyphen.dto.request.*;
import liaison.groble.external.hyphen.dto.response.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class HyphenApiClient {

  @Qualifier("hyphenWebClient")
  private final WebClient webClient;

  private final HyphenConfig hyphenConfig;

  /** FCS 계좌인증 API 호출 */
  public Mono<HyphenAccountVerificationResponse> verifyAccount(
      HyphenAccountVerificationRequest request) {
    return webClient
        .post()
        .uri(hyphenConfig.getAccountVerificationUrl())
        .headers(headers -> setCommonHeaders(headers))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(HyphenAccountVerificationResponse.class)
        .doOnSuccess(response -> log.info("계좌인증 응답: {}", response))
        .doOnError(error -> log.error("계좌인증 실패", error));
  }

  /** 1원송금 tr1 API 호출 (송금 요청) */
  public Mono<HyphenOneWonTransferResponse> requestOneWonTransfer(
      HyphenOneWonTransferRequest request) {
    return webClient
        .post()
        .uri(hyphenConfig.getOneWonTransferUrl())
        .headers(headers -> setCommonHeaders(headers))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(HyphenOneWonTransferResponse.class)
        .doOnSuccess(response -> log.info("1원송금 요청 응답: {}", response))
        .doOnError(error -> log.error("1원송금 요청 실패", error));
  }

  /** 1원송금 tr2 API 호출 (송금 검증) */
  public Mono<HyphenOneWonVerifyResponse> verifyOneWonTransfer(HyphenOneWonVerifyRequest request) {
    return webClient
        .post()
        .uri(hyphenConfig.getOneWonVerifyUrl())
        .headers(headers -> setCommonHeaders(headers))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(HyphenOneWonVerifyResponse.class)
        .doOnSuccess(response -> log.info("1원송금 검증 응답: {}", response))
        .doOnError(error -> log.error("1원송금 검증 실패", error));
  }

  /** 공통 헤더 설정 */
  private void setCommonHeaders(HttpHeaders headers) {
    headers.set("user-id", hyphenConfig.getUserId());
    headers.set("Hkey", hyphenConfig.getHkey());
    headers.set("user-tr-no", generateTransactionNo());

    // 테스트 모드일 때만 설정
    String gustationHeader = hyphenConfig.getGustationHeader();
    if (gustationHeader != null) {
      headers.set("hyphen-gustation", gustationHeader);
    }
  }

  /** 거래 고유번호 생성 */
  private String generateTransactionNo() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
