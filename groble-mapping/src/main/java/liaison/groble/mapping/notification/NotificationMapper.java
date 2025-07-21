package liaison.groble.mapping.notification;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.notification.response.NotificationDetailsResponse;
import liaison.groble.api.model.notification.response.NotificationItemResponse;
import liaison.groble.api.model.notification.response.NotificationItemsResponse;
import liaison.groble.application.notification.dto.NotificationDetailsDTO;
import liaison.groble.application.notification.dto.NotificationItemDTO;
import liaison.groble.application.notification.dto.NotificationItemsDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface NotificationMapper {

  /** 단일 상세 DTO → Response */
  NotificationDetailsResponse toNotificationDetailsResponse(
      NotificationDetailsDTO notificationDetailsDTO);

  /** 개별 알림 아이템 DTO → Response */
  NotificationItemResponse toNotificationItemResponse(NotificationItemDTO notificationItemDTO);

  /** 아이템 리스트 매핑 (MapStruct가 List<DTO> → List<Response> 자동으로) */
  List<NotificationItemResponse> toNotificationItemResponseList(
      List<NotificationItemDTO> notificationItemDTOs);

  NotificationItemsResponse toNotificationItemsResponse(NotificationItemsDTO notificationItemsDTO);
}
