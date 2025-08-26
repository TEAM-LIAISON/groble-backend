package liaison.groble.application.notification.listener;

import org.springframework.stereotype.Component;

import liaison.groble.application.notification.service.KakaoNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
  private final KakaoNotificationService kakaoNotificationService;

  //    @EventListener
  //    @Async
  //    public void handleUserSignUp(UserSignUpEvent event) {
  //        // 인앱 알림 생성
  //        inAppNotificationService.createNotification(
  //                NotificationRequest.welcome(event.getUserId(), event.getUserName())
  //        );
  //
  //        // 카카오톡 발송
  //        kakaoNotificationService.sendNotification(
  //                KakaoNotificationRequest.welcome(
  //                        event.getPhoneNumber(),
  //                        event.getUserName()
  //                )
  //        );
  //    }
}
