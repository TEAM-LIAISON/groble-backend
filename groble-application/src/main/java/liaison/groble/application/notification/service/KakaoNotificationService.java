package liaison.groble.application.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import liaison.groble.application.notification.dto.KakaoMessageDTO;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.resolver.KakaoTemplateResolver;
import liaison.groble.application.notification.template.KakaoTemplate;
import liaison.groble.application.notification.template.NotificationUrlBuilder;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;
import liaison.groble.external.infotalk.dto.message.MessageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoNotificationService {
  private final KakaoMessageSender kakaoMessageSender;
  private final KakaoTemplateResolver kakaoTemplateResolver;
  private final NotificationUrlBuilder notificationUrlBuilder;

  public void sendNotification(KakaoNotificationDTO kakaoNotificationDTO) {
    try {
      KakaoTemplate template = kakaoTemplateResolver.resolve(kakaoNotificationDTO.getType());
      String messageContent = template.buildMessage(kakaoNotificationDTO);
      List<ButtonInfo> buttons =
          template.buildButtons(kakaoNotificationDTO, notificationUrlBuilder);

      MessageResponse response =
          kakaoMessageSender.send(
              KakaoMessageDTO.builder()
                  .phoneNumber(kakaoNotificationDTO.getPhoneNumber())
                  .templateCode(template.getCode())
                  .title(template.getTitle())
                  .content(messageContent)
                  .buttons(buttons)
                  .build());

      handleResponse(response, kakaoNotificationDTO);

    } catch (Exception e) {
      log.error(
          "Failed to send Kakao notification. type: {}, user: {}",
          kakaoNotificationDTO.getType(),
          kakaoNotificationDTO.getUsername(),
          e);
      //            failedMessageRecorder.record(kakaoNotificationDTO, e);
    }
  }

  private void handleResponse(MessageResponse response, KakaoNotificationDTO kakaoNotificationDTO) {
    if (response.isSuccess()) {
      log.info(
          "Kakao message sent successfully. type: {}, messageKey: {}",
          kakaoNotificationDTO.getType(),
          response.getMessageKey());
    } else {
      log.warn(
          "Kakao message failed. type: {}, error: {}",
          kakaoNotificationDTO.getType(),
          response.getErrorMessage());
      //            failedMessageRecorder.record(kakaoNotificationDTO, response.getErrorMessage());
    }
  }
}
