package liaison.groble.api.model.notification.scheduled.response;

import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "예약 알림 세그먼트 응답")
public class ScheduledNotificationSegmentResponse {

  @Schema(description = "세그먼트 ID", example = "1")
  private Long id;

  @Schema(description = "세그먼트 이름", example = "VIP_USERS")
  private String name;

  @Schema(description = "세그먼트 설명", example = "VIP 등급 사용자 대상")
  private String description;

  @Schema(description = "세그먼트 유형", example = "CUSTOM")
  private ScheduledNotificationSegmentType segmentType;

  @Schema(description = "세그먼트 조건 JSON", example = "{\"tier\":\"VIP\"}")
  private String segmentPayload;

  @Schema(description = "활성 여부", example = "true")
  private boolean active;
}
