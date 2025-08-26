package liaison.groble.application.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.infotalk.service.BizppurioMessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoMessageSender {
  private final BizppurioMessageService bizppurioMessageService;

  @Value("${bizppurio.kakao-sender-key}")
  private String kakaoSenderKey;

  //    public MessageResponse send(KakaoMessage message) {
  //        return bizppurioMessageService.sendAlimtalk(
  //                message.getPhoneNumber(),
  //                message.getTemplateCode(),
  //                message.getTitle(),
  //                message.getContent(),
  //                kakaoSenderKey,
  //                message.getButtons()
  //        );
  //    }
}
