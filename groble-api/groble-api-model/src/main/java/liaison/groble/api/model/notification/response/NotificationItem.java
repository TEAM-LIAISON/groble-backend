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
@Schema(description = "알림 아이템")
public class NotificationItem {
  @Schema(description = "알림 ID")
  private Long notificationId;

  @Schema(description = "알림 타입")
  private String notificationType;

  @Schema(description = "알림 서브 타입")
  private String subNotificationType;

  @Schema(description = "알림 읽음 상태")
  private String notificationReadStatus;

  @Schema(description = "알림 발생 시간")
  private LocalDateTime notificationOccurTime;

  @Schema(description = "알림 상세 정보")
  private NotificationDetails notificationDetails;
}
