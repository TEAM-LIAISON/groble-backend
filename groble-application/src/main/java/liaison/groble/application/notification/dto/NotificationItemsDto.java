package liaison.groble.application.notification.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationItemsDto {
  @Builder.Default private List<NotificationItemDto> notificationItems = new ArrayList<>();
}
