package liaison.groble.api.model.notification.scheduled.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "예약 알림 통계 응답")
public class ScheduledNotificationStatisticsResponse {

  @Schema(description = "전체 예약 알림 수", example = "10")
  private long totalNotifications;

  @Schema(description = "발송 대기 수", example = "5")
  private long readyCount;

  @Schema(description = "발송 완료 수", example = "3")
  private long sentCount;

  @Schema(description = "발송 실패 수", example = "1")
  private long failedCount;

  @Schema(description = "취소된 예약 수", example = "1")
  private long cancelledCount;
}
