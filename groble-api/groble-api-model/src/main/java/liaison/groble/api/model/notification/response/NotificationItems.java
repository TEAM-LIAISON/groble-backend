package liaison.groble.api.model.notification.response;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 목록 응답")
public class NotificationItems {

  @Schema(description = "알림 목록 리스트")
  @Builder.Default
  private List<NotificationItem> notificationItems = new ArrayList<>();
}
