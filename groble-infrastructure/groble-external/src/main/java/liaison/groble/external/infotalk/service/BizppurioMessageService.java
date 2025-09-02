package liaison.groble.external.infotalk.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import liaison.groble.external.config.BizppurioConfig;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;
import liaison.groble.external.infotalk.dto.message.MessageRequest;
import liaison.groble.external.infotalk.dto.message.MessageResponse;
import liaison.groble.external.infotalk.dto.message.MessageType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비즈뿌리오 메시지 발송 서비스
 *
 * <p>이 서비스는 다양한 유형의 메시지를 발송하는 기능을 제공합니다. 토큰 관리는 BizppurioTokenService가 자동으로 처리하므로, 이 클래스는 메시지 발송
 * 로직에만 집중합니다.
 *
 * <p>주요 기능: 1. SMS/LMS/MMS 발송 2. 알림톡/친구톡 발송 3. 예약 발송 4. 대체 발송 설정 5. 자동 재시도 (네트워크 오류 시)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizppurioMessageService {
  private final BizppurioConfig config;
  private final BizppurioTokenService tokenService;
  private final RestTemplate restTemplate;

  // API 엔드포인트
  private static final String MESSAGE_ENDPOINT = "/v3/message";

  /**
   * 알림톡 발송 - 버튼 포함 버전
   *
   * @param to 수신번호
   * @param templateCode 템플릿 코드
   * @param content 메시지 내용 (템플릿 변수 치환 완료)
   * @param senderKey 발신프로필키
   * @param buttons 버튼 정보 (선택)
   * @return 발송 응답
   */
  public MessageResponse sendAlimtalk(
      String to,
      String templateCode,
      String title,
      String content,
      String senderKey,
      List<ButtonInfo> buttons) {

    // 1. 알림톡 메시지 구조 생성
    MessageRequest.AtMessage atMessage =
        MessageRequest.AtMessage.builder()
            .message(content) // 메시지 내용
            .senderkey(senderKey) // 발신프로필키 (필수)
            .templatecode(templateCode) // 템플릿 코드 (필수)
            .button(buttons) // 버튼 (선택)
            .title(title)
            .build();

    // 2. AtContent로 감싸기
    MessageRequest.AtContent atContent = MessageRequest.AtContent.builder().at(atMessage).build();

    // 3. 메시지 요청 생성
    MessageRequest request =
        MessageRequest.builder()
            .account(config.getAccount())
            .type(MessageType.ALIMTALK.getCode()) // "at"
            .from(config.getDefaultSender()) // 발신번호
            .to(formatPhoneNumber(to)) // 수신번호
            .content(atContent) // AtContent 객체 사용
            .refKey(generateRefKey()) // 고유 참조키
            .build();

    log.debug("알림톡 요청 생성 - Template: {}, SenderKey: {}", templateCode, senderKey);
    return sendMessage(request);
  }

  /**
   * 실제 메시지 발송 처리 @Retryable 어노테이션으로 자동 재시도를 구현합니다. 네트워크 오류나 일시적인 서버 오류 시 자동으로 재시도합니다.
   *
   * @param request 메시지 요청
   * @return 발송 응답
   */
  @Retryable(
      value = {HttpServerErrorException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  private MessageResponse sendMessage(MessageRequest request) {
    try {
      // 1. Bearer 토큰 획득 (자동 갱신)
      String token = tokenService.getValidToken();

      // 2. HTTP 헤더 구성
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(token); // "Bearer " + token 형태로 설정

      // 3. 요청 엔티티 생성
      HttpEntity<MessageRequest> httpEntity = new HttpEntity<>(request, headers);

      // 4. API URL 구성
      String url = config.getBaseUrl() + MESSAGE_ENDPOINT;

      // 로깅 (민감정보는 일부만 표시)
      log.info(
          "메시지 발송 요청 - Type: {}, To: {}, RefKey: {}",
          request.getType(),
          maskPhoneNumber(request.getTo()),
          request.getRefKey());

      // 5. API 호출
      ResponseEntity<MessageResponse> responseEntity =
          restTemplate.exchange(url, HttpMethod.POST, httpEntity, MessageResponse.class);

      // 6. 응답 처리
      MessageResponse response = responseEntity.getBody();

      if (response != null && response.isSuccess()) {
        log.info(
            "메시지 발송 API 성공 - MessageKey: {}, RefKey: {}",
            response.getMessageKey(),
            response.getRefKey());

        // 중요: 여기서 성공은 API 호출 성공이지, 실제 발송 성공이 아닙니다!
        // 실제 발송 결과는 리포트 API나 웹훅을 통해 확인해야 합니다.
        log.debug("주의: API 성공은 발송 완료를 의미하지 않습니다. 발송 결과는 별도 확인 필요.");

      } else if (response != null) {
        log.error("메시지 발송 API 실패 - {}", response.getErrorMessage());
      }

      return response;

    } catch (HttpClientErrorException e) {
      // 4xx 에러 (클라이언트 오류)
      log.error("메시지 발송 실패 - 클라이언트 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new RuntimeException("메시지 발송 실패: " + e.getMessage(), e);

    } catch (HttpServerErrorException e) {
      // 5xx 에러 (서버 오류) - 재시도 대상
      log.error("메시지 발송 실패 - 서버 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw e; // @Retryable에 의해 재시도됨

    } catch (Exception e) {
      // 기타 오류
      log.error("메시지 발송 중 예외 발생", e);
      throw new RuntimeException("메시지 발송 실패", e);
    }
  }

  /**
   * 고유한 참조키 생성
   *
   * <p>RefKey는 중복 발송을 방지하는 중요한 역할을 합니다. 같은 RefKey로 재발송하면 비즈뿌리오에서 중복으로 처리합니다.
   *
   * @return 고유 참조키 (32자)
   */
  private String generateRefKey() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * 전화번호 포맷팅
   *
   * <p>하이픈을 제거하고 숫자만 남깁니다.
   *
   * @param phoneNumber 원본 전화번호
   * @return 포맷팅된 전화번호
   */
  private String formatPhoneNumber(String phoneNumber) {
    if (phoneNumber == null) {
      throw new IllegalArgumentException("전화번호는 필수입니다.");
    }

    // 하이픈, 공백, 괄호 등 제거
    String formatted = phoneNumber.replaceAll("[^0-9]", "");

    // 한국 번호 검증 (01로 시작하는 10-11자리)
    if (!formatted.matches("^01[0-9]{8,9}$")) {
      throw new IllegalArgumentException("올바른 휴대폰 번호 형식이 아닙니다: " + phoneNumber);
    }

    return formatted;
  }

  /**
   * 전화번호 마스킹 (로깅용)
   *
   * <p>개인정보 보호를 위해 전화번호 일부를 마스킹합니다.
   *
   * @param phoneNumber 전화번호
   * @return 마스킹된 전화번호
   */
  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 8) {
      return "****";
    }

    int length = phoneNumber.length();
    return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(length - 4);
  }
}
