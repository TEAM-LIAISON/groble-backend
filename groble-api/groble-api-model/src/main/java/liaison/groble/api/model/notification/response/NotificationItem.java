package liaison.groble.api.model.notification.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 단일 응답")
public class NotificationItem {
  private Long notificationId;

  private String notificationType;
  private String subNotificationType;
  private LocalDateTime notificationOccurTime;

  private NotificationDetails notificationDetails;
}
