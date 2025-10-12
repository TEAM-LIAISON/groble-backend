package liaison.groble.api.model.notification.scheduled.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSendType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "예약 알림 수정 요청")
public class UpdateScheduledNotificationRequest {

  @NotNull
  @Schema(description = "발송 채널", example = "SYSTEM")
  private ScheduledNotificationChannel channel;

  @Schema(description = "발송 유형", example = "ONCE")
  private ScheduledNotificationSendType sendType;

  @Schema(description = "상태 값", example = "READY")
  private ScheduledNotificationStatus status;

  @Schema(description = "알림 제목", example = "정기 점검 안내")
  private String title;

  @Schema(description = "알림 내용", example = "9월 1일 02시에 시스템 점검이 진행됩니다.")
  private String content;

  @Schema(description = "카카오 알림톡 템플릿 코드", example = "bizp_2025082019070532533937376")
  private String bizTemplateCode;

  @Schema(description = "카카오 발신 프로필 키", example = "a3861a5e4d576905f758d6c9b5459f9d00202952")
  private String bizSenderKey;

  @NotNull
  @Future
  @Schema(description = "예약 발송 시각", example = "2025-10-10T09:00:00")
  private LocalDateTime scheduledAt;

  @Schema(description = "반복 발송 크론 표현식", example = "0 0 9 * * ?")
  private String repeatCron;

  @Schema(description = "세그먼트 유형", example = "ALL_USERS")
  private ScheduledNotificationSegmentType segmentType;

  @Schema(description = "세그먼트 조건 JSON", example = "{\"tier\":\"VIP\"}")
  private String segmentPayload;

  @Schema(description = "발송 타임존", example = "Asia/Seoul")
  private String timezone;
}
