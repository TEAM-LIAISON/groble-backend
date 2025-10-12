package liaison.groble.application.notification.scheduled.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.notification.scheduled.command.CreateScheduledNotificationCommand;
import liaison.groble.application.notification.scheduled.command.UpdateScheduledNotificationCommand;
import liaison.groble.application.notification.scheduled.dto.KakaoTemplateDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationChannelStatisticsDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationSegmentDTO;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationStatisticsDTO;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.exception.InvalidRequestException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.notification.scheduled.dto.ScheduledNotificationChannelAggregate;
import liaison.groble.domain.notification.scheduled.dto.ScheduledNotificationStatisticsAggregate;
import liaison.groble.domain.notification.scheduled.entity.ScheduledNotification;
import liaison.groble.domain.notification.scheduled.entity.ScheduledNotificationSegment;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationChannel;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSegmentType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationSendType;
import liaison.groble.domain.notification.scheduled.enums.ScheduledNotificationStatus;
import liaison.groble.domain.notification.scheduled.repository.ScheduledNotificationRepository;
import liaison.groble.domain.notification.scheduled.repository.ScheduledNotificationSegmentRepository;
import liaison.groble.external.config.BizppurioConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledNotificationAdminService {

  private static final String DEFAULT_TIMEZONE = "Asia/Seoul";
  private static final List<ScheduledNotificationChannel> CHANNELS_FOR_RESPONSE =
      List.of(ScheduledNotificationChannel.SYSTEM, ScheduledNotificationChannel.KAKAO_BIZ);

  private final ScheduledNotificationRepository scheduledNotificationRepository;
  private final ScheduledNotificationSegmentRepository scheduledNotificationSegmentRepository;
  private final BizppurioConfig bizppurioConfig;

  @Transactional
  public ScheduledNotificationDTO createScheduledNotification(
      CreateScheduledNotificationCommand command) {
    validateChannel(command.getChannel(), command.getBizTemplateCode());

    ScheduledNotificationSendType sendType =
        Optional.ofNullable(command.getSendType()).orElse(ScheduledNotificationSendType.ONCE);
    ScheduledNotificationSegmentType segmentType =
        Optional.ofNullable(command.getSegmentType())
            .orElse(ScheduledNotificationSegmentType.ALL_USERS);

    ScheduledNotification entity =
        ScheduledNotification.builder()
            .channel(command.getChannel())
            .sendType(sendType)
            .status(resolveInitialStatus(sendType))
            .title(command.getTitle())
            .content(command.getContent())
            .bizTemplateCode(command.getBizTemplateCode())
            .bizSenderKey(command.getBizSenderKey())
            .scheduledAt(command.getScheduledAt())
            .repeatCron(command.getRepeatCron())
            .segmentType(segmentType)
            .segmentPayload(resolveSegmentPayload(segmentType, command.getSegmentPayload()))
            .timezone(Optional.ofNullable(command.getTimezone()).orElse(DEFAULT_TIMEZONE))
            .createdByAdminId(command.getAdminId())
            .build();

    ScheduledNotification saved = scheduledNotificationRepository.save(entity);
    return toDto(saved);
  }

  @Transactional
  public ScheduledNotificationDTO updateScheduledNotification(
      UpdateScheduledNotificationCommand command) {
    ScheduledNotification scheduledNotification = getScheduledNotification(command.getId());
    validateChannel(command.getChannel(), command.getBizTemplateCode());

    ScheduledNotificationStatus nextStatus =
        Optional.ofNullable(command.getStatus()).orElse(scheduledNotification.getStatus());

    scheduledNotification.update(
        command.getChannel(),
        Optional.ofNullable(command.getSendType()).orElse(scheduledNotification.getSendType()),
        nextStatus,
        command.getTitle(),
        command.getContent(),
        command.getBizTemplateCode(),
        command.getBizSenderKey(),
        command.getScheduledAt(),
        command.getRepeatCron(),
        Optional.ofNullable(command.getSegmentType())
            .orElse(scheduledNotification.getSegmentType()),
        resolveSegmentPayload(
            Optional.ofNullable(command.getSegmentType())
                .orElse(scheduledNotification.getSegmentType()),
            command.getSegmentPayload()),
        Optional.ofNullable(command.getTimezone()).orElse(scheduledNotification.getTimezone()),
        command.getAdminId());

    return toDto(scheduledNotification);
  }

  @Transactional
  public void cancelScheduledNotification(Long id, Long adminId) {
    ScheduledNotification scheduledNotification = getScheduledNotification(id);
    if (scheduledNotification.getStatus() == ScheduledNotificationStatus.CANCELLED) {
      throw new InvalidRequestException("이미 취소된 예약 알림입니다.");
    }
    scheduledNotification.markCancelled(adminId);
  }

  public PageResponse<ScheduledNotificationDTO> getScheduledNotifications(Pageable pageable) {
    Page<ScheduledNotification> page = scheduledNotificationRepository.findAll(pageable);
    List<ScheduledNotificationDTO> items = page.stream().map(this::toDto).toList();
    return PageResponse.from(page, items);
  }

  public List<ScheduledNotificationSegmentDTO> getSegments() {
    List<ScheduledNotificationSegmentDTO> segments = new ArrayList<>();
    for (ScheduledNotificationSegmentType type : ScheduledNotificationSegmentType.values()) {
      if (type == ScheduledNotificationSegmentType.CUSTOM) {
        continue;
      }
      segments.add(
          ScheduledNotificationSegmentDTO.builder()
              .id(null)
              .name(type.name())
              .description("사전 정의된 세그먼트")
              .segmentType(type)
              .segmentPayload(null)
              .active(true)
              .build());
    }

    scheduledNotificationSegmentRepository.findActiveSegments().stream()
        .map(this::toSegmentDto)
        .forEach(segments::add);

    return segments;
  }

  public List<KakaoTemplateDTO> getKakaoTemplates() {
    Map<String, BizppurioConfig.Template> templates = bizppurioConfig.getTemplates();
    if (templates == null || templates.isEmpty()) {
      return List.of();
    }
    return templates.entrySet().stream()
        .map(
            entry ->
                KakaoTemplateDTO.builder()
                    .key(entry.getKey())
                    .code(entry.getValue().getCode())
                    .name(Optional.ofNullable(entry.getValue().getName()).orElse(entry.getKey()))
                    .build())
        .toList();
  }

  public ScheduledNotificationStatisticsDTO getStatistics(
      LocalDate startDate, LocalDate endDate, String channelParam) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new InvalidRequestException("조회 종료일은 시작일 이후여야 합니다.");
    }

    LocalDateTime startDateTime = toStartOfDay(startDate);
    LocalDateTime endDateTime = toEndOfDay(endDate);
    ScheduledNotificationChannel channelFilter = parseChannel(channelParam);

    ScheduledNotificationStatisticsAggregate aggregate =
        scheduledNotificationRepository.aggregateStatistics(
            startDateTime, endDateTime, channelFilter);
    List<ScheduledNotificationChannelAggregate> channelAggregates =
        scheduledNotificationRepository.aggregateStatisticsByChannel(
            startDateTime, endDateTime, channelFilter);

    Map<ScheduledNotificationChannel, ScheduledNotificationChannelStatisticsDTO> channelStats =
        new EnumMap<>(ScheduledNotificationChannel.class);
    CHANNELS_FOR_RESPONSE.forEach(channel -> channelStats.put(channel, buildChannelStats(0L, 0L)));

    for (ScheduledNotificationChannelAggregate channelAggregate : channelAggregates) {
      if (!channelStats.containsKey(channelAggregate.channel())) {
        continue;
      }
      channelStats.put(
          channelAggregate.channel(),
          buildChannelStats(channelAggregate.totalScheduled(), channelAggregate.totalSent()));
    }

    ScheduledNotificationStatisticsDTO.ChannelStatisticsDTO channelStatisticsDTO =
        ScheduledNotificationStatisticsDTO.ChannelStatisticsDTO.builder()
            .system(
                channelStats.getOrDefault(
                    ScheduledNotificationChannel.SYSTEM, buildChannelStats(0L, 0L)))
            .kakao(
                channelStats.getOrDefault(
                    ScheduledNotificationChannel.KAKAO_BIZ, buildChannelStats(0L, 0L)))
            .build();

    return ScheduledNotificationStatisticsDTO.builder()
        .totalScheduled(aggregate.totalScheduled())
        .totalSent(aggregate.totalSent())
        .totalCancelled(aggregate.totalCancelled())
        .deliveryRate(calculateDeliveryRate(aggregate.totalSent(), aggregate.totalScheduled()))
        .channelStats(channelStatisticsDTO)
        .build();
  }

  private ScheduledNotification getScheduledNotification(Long id) {
    return scheduledNotificationRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("예약 알림을 찾을 수 없습니다. id=" + id));
  }

  private ScheduledNotificationDTO toDto(ScheduledNotification entity) {
    return ScheduledNotificationDTO.builder()
        .id(entity.getId())
        .channel(entity.getChannel())
        .sendType(entity.getSendType())
        .status(entity.getStatus())
        .title(entity.getTitle())
        .content(entity.getContent())
        .bizTemplateCode(entity.getBizTemplateCode())
        .bizSenderKey(entity.getBizSenderKey())
        .scheduledAt(entity.getScheduledAt())
        .repeatCron(entity.getRepeatCron())
        .segmentType(entity.getSegmentType())
        .segmentPayload(entity.getSegmentPayload())
        .timezone(entity.getTimezone())
        .createdByAdminId(entity.getCreatedByAdminId())
        .updatedByAdminId(entity.getUpdatedByAdminId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private ScheduledNotificationSegmentDTO toSegmentDto(ScheduledNotificationSegment segment) {
    return ScheduledNotificationSegmentDTO.builder()
        .id(segment.getId())
        .name(segment.getName())
        .description(segment.getDescription())
        .segmentType(segment.getSegmentType())
        .segmentPayload(segment.getSegmentPayload())
        .active(segment.isActive())
        .build();
  }

  private String resolveSegmentPayload(
      ScheduledNotificationSegmentType segmentType, String segmentPayload) {
    if (segmentType == ScheduledNotificationSegmentType.CUSTOM) {
      return segmentPayload;
    }
    return null;
  }

  private ScheduledNotificationChannelStatisticsDTO buildChannelStats(long scheduled, long sent) {
    return ScheduledNotificationChannelStatisticsDTO.builder()
        .scheduled(scheduled)
        .sent(sent)
        .deliveryRate(calculateDeliveryRate(sent, scheduled))
        .build();
  }

  private double calculateDeliveryRate(long sent, long scheduled) {
    if (scheduled == 0) {
      return 0.0;
    }
    return BigDecimal.valueOf(sent)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(scheduled), 1, RoundingMode.HALF_UP)
        .doubleValue();
  }

  private ScheduledNotificationChannel parseChannel(String channelParam) {
    if (channelParam == null || channelParam.isBlank() || channelParam.equalsIgnoreCase("all")) {
      return null;
    }
    if (channelParam.equalsIgnoreCase("system")) {
      return ScheduledNotificationChannel.SYSTEM;
    }
    if (channelParam.equalsIgnoreCase("kakao")) {
      return ScheduledNotificationChannel.KAKAO_BIZ;
    }
    throw new InvalidRequestException("지원하지 않는 채널 필터입니다.");
  }

  private LocalDateTime toStartOfDay(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  private LocalDateTime toEndOfDay(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.atTime(LocalTime.of(23, 59, 59, 999_999_000));
  }

  private void validateChannel(ScheduledNotificationChannel channel, String bizTemplateCode) {
    if (channel == null) {
      throw new InvalidRequestException("알림 채널은 필수 항목입니다.");
    }
    if (channel == ScheduledNotificationChannel.KAKAO_BIZ
        && (bizTemplateCode == null || bizTemplateCode.isBlank())) {
      throw new InvalidRequestException("카카오 알림톡 채널은 템플릿 코드를 필수로 입력해야 합니다.");
    }
  }

  private ScheduledNotificationStatus resolveInitialStatus(ScheduledNotificationSendType sendType) {
    if (sendType == ScheduledNotificationSendType.RECURRING) {
      return ScheduledNotificationStatus.READY;
    }
    return ScheduledNotificationStatus.READY;
  }
}
