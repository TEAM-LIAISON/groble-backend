package liaison.groble.mapping.notification;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.notification.scheduled.request.CreateScheduledNotificationRequest;
import liaison.groble.api.model.notification.scheduled.request.UpdateScheduledNotificationRequest;
import liaison.groble.api.model.notification.scheduled.response.KakaoTemplateResponse;
import liaison.groble.api.model.notification.scheduled.response.ScheduledNotificationResponse;
import liaison.groble.api.model.notification.scheduled.response.ScheduledNotificationSegmentResponse;
import liaison.groble.api.model.notification.scheduled.response.ScheduledNotificationStatisticsResponse;
import liaison.groble.application.notification.scheduled.command.CreateScheduledNotificationCommand;
import liaison.groble.application.notification.scheduled.command.UpdateScheduledNotificationCommand;
import liaison.groble.application.notification.scheduled.dto.KakaoTemplateDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationChannelStatisticsDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationSegmentDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationStatisticsDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ScheduledNotificationAdminMapper extends PageResponseMapper {

  ScheduledNotificationResponse toResponse(ScheduledNotificationDTO dto);

  default PageResponse<ScheduledNotificationResponse> toScheduledNotificationResponsePage(
      PageResponse<ScheduledNotificationDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toResponse);
  }

  default CreateScheduledNotificationCommand toCreateCommand(
      CreateScheduledNotificationRequest request, Long adminId) {
    if (request == null) {
      return null;
    }
    return CreateScheduledNotificationCommand.builder()
        .channel(request.getChannel())
        .sendType(request.getSendType())
        .title(request.getTitle())
        .content(request.getContent())
        .bizTemplateCode(request.getBizTemplateCode())
        .bizSenderKey(request.getBizSenderKey())
        .scheduledAt(request.getScheduledAt())
        .repeatCron(request.getRepeatCron())
        .segmentType(request.getSegmentType())
        .segmentPayload(request.getSegmentPayload())
        .timezone(request.getTimezone())
        .adminId(adminId)
        .build();
  }

  default UpdateScheduledNotificationCommand toUpdateCommand(
      UpdateScheduledNotificationRequest request, Long id, Long adminId) {
    if (request == null) {
      return null;
    }
    return UpdateScheduledNotificationCommand.builder()
        .id(id)
        .channel(request.getChannel())
        .sendType(request.getSendType())
        .status(request.getStatus())
        .title(request.getTitle())
        .content(request.getContent())
        .bizTemplateCode(request.getBizTemplateCode())
        .bizSenderKey(request.getBizSenderKey())
        .scheduledAt(request.getScheduledAt())
        .repeatCron(request.getRepeatCron())
        .segmentType(request.getSegmentType())
        .segmentPayload(request.getSegmentPayload())
        .timezone(request.getTimezone())
        .adminId(adminId)
        .build();
  }

  ScheduledNotificationSegmentResponse toSegmentResponse(ScheduledNotificationSegmentDTO dto);

  List<ScheduledNotificationSegmentResponse> toSegmentResponses(
      List<ScheduledNotificationSegmentDTO> dtoList);

  KakaoTemplateResponse toTemplateResponse(KakaoTemplateDTO dto);

  List<KakaoTemplateResponse> toTemplateResponses(List<KakaoTemplateDTO> dtoList);

  default ScheduledNotificationStatisticsResponse toStatisticsResponse(
      ScheduledNotificationStatisticsDTO dto) {
    if (dto == null) {
      return null;
    }

    ScheduledNotificationStatisticsDTO.ChannelStatisticsDTO channelStats = dto.getChannelStats();

    ScheduledNotificationStatisticsResponse.ChannelStatsResponse channelStatsResponse =
        ScheduledNotificationStatisticsResponse.ChannelStatsResponse.builder()
            .system(toChannelStatResponse(channelStats != null ? channelStats.getSystem() : null))
            .kakao(toChannelStatResponse(channelStats != null ? channelStats.getKakao() : null))
            .build();

    return ScheduledNotificationStatisticsResponse.builder()
        .totalScheduled(dto.getTotalScheduled())
        .totalSent(dto.getTotalSent())
        .totalCancelled(dto.getTotalCancelled())
        .deliveryRate(dto.getDeliveryRate())
        .channelStats(channelStatsResponse)
        .build();
  }

  private ScheduledNotificationStatisticsResponse.ChannelStatResponse toChannelStatResponse(
      ScheduledNotificationChannelStatisticsDTO dto) {
    if (dto == null) {
      return ScheduledNotificationStatisticsResponse.ChannelStatResponse.builder()
          .scheduled(0L)
          .sent(0L)
          .deliveryRate(0.0)
          .build();
    }
    return ScheduledNotificationStatisticsResponse.ChannelStatResponse.builder()
        .scheduled(dto.getScheduled())
        .sent(dto.getSent())
        .deliveryRate(dto.getDeliveryRate())
        .build();
  }
}
