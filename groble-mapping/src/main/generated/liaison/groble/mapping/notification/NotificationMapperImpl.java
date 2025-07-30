package liaison.groble.mapping.notification;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.notification.response.NotificationDetailsResponse;
import liaison.groble.api.model.notification.response.NotificationItemResponse;
import liaison.groble.api.model.notification.response.NotificationItemsResponse;
import liaison.groble.application.notification.dto.NotificationDetailsDTO;
import liaison.groble.application.notification.dto.NotificationItemDTO;
import liaison.groble.application.notification.dto.NotificationItemsDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-30T17:38:21+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class NotificationMapperImpl implements NotificationMapper {

  @Override
  public NotificationDetailsResponse toNotificationDetailsResponse(
      NotificationDetailsDTO notificationDetailsDTO) {
    if (notificationDetailsDTO == null) {
      return null;
    }

    NotificationDetailsResponse.NotificationDetailsResponseBuilder notificationDetailsResponse =
        NotificationDetailsResponse.builder();

    if (notificationDetailsDTO.getNickname() != null) {
      notificationDetailsResponse.nickname(notificationDetailsDTO.getNickname());
    }
    if (notificationDetailsDTO.getContentId() != null) {
      notificationDetailsResponse.contentId(notificationDetailsDTO.getContentId());
    }
    if (notificationDetailsDTO.getThumbnailUrl() != null) {
      notificationDetailsResponse.thumbnailUrl(notificationDetailsDTO.getThumbnailUrl());
    }
    if (notificationDetailsDTO.getSystemTitle() != null) {
      notificationDetailsResponse.systemTitle(notificationDetailsDTO.getSystemTitle());
    }

    return notificationDetailsResponse.build();
  }

  @Override
  public NotificationItemResponse toNotificationItemResponse(
      NotificationItemDTO notificationItemDTO) {
    if (notificationItemDTO == null) {
      return null;
    }

    NotificationItemResponse.NotificationItemResponseBuilder notificationItemResponse =
        NotificationItemResponse.builder();

    if (notificationItemDTO.getNotificationId() != null) {
      notificationItemResponse.notificationId(notificationItemDTO.getNotificationId());
    }
    if (notificationItemDTO.getNotificationType() != null) {
      notificationItemResponse.notificationType(notificationItemDTO.getNotificationType());
    }
    if (notificationItemDTO.getSubNotificationType() != null) {
      notificationItemResponse.subNotificationType(notificationItemDTO.getSubNotificationType());
    }
    if (notificationItemDTO.getNotificationReadStatus() != null) {
      notificationItemResponse.notificationReadStatus(
          notificationItemDTO.getNotificationReadStatus());
    }
    if (notificationItemDTO.getNotificationOccurTime() != null) {
      notificationItemResponse.notificationOccurTime(
          notificationItemDTO.getNotificationOccurTime());
    }
    if (notificationItemDTO.getNotificationDetails() != null) {
      notificationItemResponse.notificationDetails(
          toNotificationDetailsResponse(notificationItemDTO.getNotificationDetails()));
    }

    return notificationItemResponse.build();
  }

  @Override
  public List<NotificationItemResponse> toNotificationItemResponseList(
      List<NotificationItemDTO> notificationItemDTOs) {
    if (notificationItemDTOs == null) {
      return null;
    }

    List<NotificationItemResponse> list =
        new ArrayList<NotificationItemResponse>(notificationItemDTOs.size());
    for (NotificationItemDTO notificationItemDTO : notificationItemDTOs) {
      list.add(toNotificationItemResponse(notificationItemDTO));
    }

    return list;
  }

  @Override
  public NotificationItemsResponse toNotificationItemsResponse(
      NotificationItemsDTO notificationItemsDTO) {
    if (notificationItemsDTO == null) {
      return null;
    }

    NotificationItemsResponse.NotificationItemsResponseBuilder notificationItemsResponse =
        NotificationItemsResponse.builder();

    List<NotificationItemResponse> list =
        toNotificationItemResponseList(notificationItemsDTO.getNotificationItems());
    if (list != null) {
      notificationItemsResponse.notificationItems(list);
    }

    return notificationItemsResponse.build();
  }
}
