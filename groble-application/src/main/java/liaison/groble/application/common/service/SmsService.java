package liaison.groble.application.common.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.common.enums.SmsTemplate;
import liaison.groble.external.sms.Message;
import liaison.groble.external.sms.SmsSender;
import liaison.groble.external.sms.exception.SmsSendException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
  private final SmsSender smsSender;

  // 템플릿 기반 SMS 전송
  public void sendSms(String phoneNumber, SmsTemplate template, Object... args) {
    String content = template.format(args);
    send(phoneNumber, content);
  }

  // 비동기 SMS 전송
  @Async
  public CompletableFuture<Void> sendSmsAsync(
      String phoneNumber, SmsTemplate template, Object... args) {
    String content = template.format(args);
    send(phoneNumber, content);
    return CompletableFuture.completedFuture(null);
  }

  // 실제 전송 로직
  private void send(String phoneNumber, String content) {
    Message message = Message.builder().to(phoneNumber).content(content).build();

    try {
      log.info(
          "SMS 전송 시도: to={}, type={}",
          phoneNumber,
          content.substring(0, Math.min(20, content.length())));
      smsSender.sendSms(message);
      log.info("SMS 전송 성공: phoneNumber={}", phoneNumber);
    } catch (Exception e) {
      log.error("SMS 전송 실패: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
      throw new SmsSendException();
    }
  }
}
