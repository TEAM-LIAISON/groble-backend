package liaison.groble.api.model.notification.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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

  @Schema(
      description = "알림 타입",
      allowableValues = {"SELLER", "CONTENT", "SYSTEM"})
  private String notificationType;

  @Schema(
      description = "알림 서브 타입",
      allowableValues = {
        "SELLER_VERIFIED",
        "SELLER_REJECTED",
        "CONTENT_APPROVED",
        "CONTENT_REJECTED",
        "WELCOME_GROBLE"
      })
  private String subNotificationType;

  @Schema(
      description = "알림 읽음 상태",
      allowableValues = {"READ", "UNREAD"})
  private String notificationReadStatus;

  @Schema(description = "알림 발생 시간 (상대적 시간 표시, 예: '3일 전')")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime notificationOccurTime;

  @Schema(description = "알림 상세 정보")
  private NotificationDetails notificationDetails;
}
