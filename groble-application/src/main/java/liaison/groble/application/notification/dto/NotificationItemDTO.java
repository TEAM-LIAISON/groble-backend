package liaison.groble.application.notification.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationItemDTO {
  private Long notificationId;
  private String notificationType; // enum 대신 String 사용
  private String subNotificationType; // enum 대신 String 사용
  private String notificationReadStatus; // enum 대신 String 사용
  private LocalDateTime notificationOccurTime; // LocalDateTime 대신 String (이미 포맷팅된 시간)
  private NotificationDetailsDTO notificationDetails; // 이름 수정
}
