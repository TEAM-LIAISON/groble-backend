package liaison.groble.api.server.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.notification.scheduled.request.CreateScheduledNotificationRequest;
import liaison.groble.api.model.notification.scheduled.request.UpdateScheduledNotificationRequest;
import liaison.groble.api.model.notification.scheduled.response.KakaoTemplateResponse;
import liaison.groble.api.model.notification.scheduled.response.ScheduledNotificationResponse;
import liaison.groble.api.model.notification.scheduled.response.ScheduledNotificationSegmentResponse;
import liaison.groble.api.model.notification.scheduled.response.ScheduledNotificationStatisticsResponse;
import liaison.groble.api.server.admin.docs.AdminNotificationSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.notification.scheduled.command.CreateScheduledNotificationCommand;
import liaison.groble.application.notification.scheduled.command.UpdateScheduledNotificationCommand;
import liaison.groble.application.notification.scheduled.dto.ScheduledNotificationDTO;
import liaison.groble.application.notification.scheduled.service.ScheduledNotificationAdminService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.exception.InvalidRequestException;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.notification.ScheduledNotificationAdminMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@Tag(
    name = AdminNotificationSwaggerDocs.TAG_NAME,
    description = AdminNotificationSwaggerDocs.TAG_DESCRIPTION)
public class AdminScheduledNotificationController extends BaseController {

  private final ScheduledNotificationAdminService scheduledNotificationAdminService;
  private final ScheduledNotificationAdminMapper scheduledNotificationAdminMapper;

  public AdminScheduledNotificationController(
      ResponseHelper responseHelper,
      ScheduledNotificationAdminService scheduledNotificationAdminService,
      ScheduledNotificationAdminMapper scheduledNotificationAdminMapper) {
    super(responseHelper);
    this.scheduledNotificationAdminService = scheduledNotificationAdminService;
    this.scheduledNotificationAdminMapper = scheduledNotificationAdminMapper;
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.CREATE_SCHEDULED_NOTIFICATION_SUMMARY,
      description = AdminNotificationSwaggerDocs.CREATE_SCHEDULED_NOTIFICATION_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.NOTIFICATIONS_SCHEDULED)
  public ResponseEntity<GrobleResponse<ScheduledNotificationResponse>> createScheduledNotification(
      @Auth Accessor accessor, @Valid @RequestBody CreateScheduledNotificationRequest request) {

    CreateScheduledNotificationCommand command =
        scheduledNotificationAdminMapper.toCreateCommand(request, accessor.getUserId());

    ScheduledNotificationDTO dto =
        scheduledNotificationAdminService.createScheduledNotification(command);
    ScheduledNotificationResponse response = scheduledNotificationAdminMapper.toResponse(dto);

    return success(response, ResponseMessages.Admin.SCHEDULED_NOTIFICATION_CREATED);
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.GET_SCHEDULED_NOTIFICATIONS_SUMMARY,
      description = AdminNotificationSwaggerDocs.GET_SCHEDULED_NOTIFICATIONS_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.NOTIFICATIONS_SCHEDULED)
  public ResponseEntity<GrobleResponse<PageResponse<ScheduledNotificationResponse>>>
      getScheduledNotifications(
          @Auth Accessor accessor,
          @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
              @RequestParam(value = "page", defaultValue = "0")
              int page,
          @Parameter(description = "페이지당 개수", example = "20")
              @RequestParam(value = "size", defaultValue = "20")
              int size,
          @Parameter(description = "정렬 기준 (property,direction)", example = "scheduledAt,desc")
              @RequestParam(value = "sort", defaultValue = "scheduledAt,desc")
              String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ScheduledNotificationDTO> dtoPage =
        scheduledNotificationAdminService.getScheduledNotifications(pageable);
    PageResponse<ScheduledNotificationResponse> responsePage =
        scheduledNotificationAdminMapper.toScheduledNotificationResponsePage(dtoPage);

    return success(responsePage, ResponseMessages.Admin.SCHEDULED_NOTIFICATIONS_RETRIEVED);
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.UPDATE_SCHEDULED_NOTIFICATION_SUMMARY,
      description = AdminNotificationSwaggerDocs.UPDATE_SCHEDULED_NOTIFICATION_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PutMapping(ApiPaths.Admin.NOTIFICATIONS_SCHEDULED_ID)
  public ResponseEntity<GrobleResponse<ScheduledNotificationResponse>> updateScheduledNotification(
      @Auth Accessor accessor,
      @PathVariable("id") Long id,
      @Valid @RequestBody UpdateScheduledNotificationRequest request) {

    UpdateScheduledNotificationCommand command =
        scheduledNotificationAdminMapper.toUpdateCommand(request, id, accessor.getUserId());

    ScheduledNotificationDTO dto =
        scheduledNotificationAdminService.updateScheduledNotification(command);
    ScheduledNotificationResponse response = scheduledNotificationAdminMapper.toResponse(dto);

    return success(response, ResponseMessages.Admin.SCHEDULED_NOTIFICATION_UPDATED);
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.CANCEL_SCHEDULED_NOTIFICATION_SUMMARY,
      description = AdminNotificationSwaggerDocs.CANCEL_SCHEDULED_NOTIFICATION_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @DeleteMapping(ApiPaths.Admin.NOTIFICATIONS_SCHEDULED_ID)
  public ResponseEntity<GrobleResponse<Void>> cancelScheduledNotification(
      @Auth Accessor accessor, @PathVariable("id") Long id) {
    scheduledNotificationAdminService.cancelScheduledNotification(id, accessor.getUserId());
    return successVoid(ResponseMessages.Admin.SCHEDULED_NOTIFICATION_CANCELLED);
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.GET_SEGMENTS_SUMMARY,
      description = AdminNotificationSwaggerDocs.GET_SEGMENTS_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.NOTIFICATIONS_SEGMENTS)
  public ResponseEntity<GrobleResponse<List<ScheduledNotificationSegmentResponse>>> getSegments(
      @Auth Accessor accessor) {
    List<ScheduledNotificationSegmentResponse> responses =
        scheduledNotificationAdminMapper.toSegmentResponses(
            scheduledNotificationAdminService.getSegments());
    return success(responses, ResponseMessages.Admin.NOTIFICATION_SEGMENTS_RETRIEVED);
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.GET_KAKAO_TEMPLATES_SUMMARY,
      description = AdminNotificationSwaggerDocs.GET_KAKAO_TEMPLATES_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.NOTIFICATIONS_KAKAO_TEMPLATES)
  public ResponseEntity<GrobleResponse<List<KakaoTemplateResponse>>> getKakaoTemplates(
      @Auth Accessor accessor) {
    List<KakaoTemplateResponse> responses =
        scheduledNotificationAdminMapper.toTemplateResponses(
            scheduledNotificationAdminService.getKakaoTemplates());
    return success(responses, ResponseMessages.Admin.KAKAO_TEMPLATES_RETRIEVED);
  }

  @Operation(
      summary = AdminNotificationSwaggerDocs.GET_STATISTICS_SUMMARY,
      description = AdminNotificationSwaggerDocs.GET_STATISTICS_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.NOTIFICATIONS_STATISTICS)
  public ResponseEntity<GrobleResponse<ScheduledNotificationStatisticsResponse>> getStatistics(
      @Auth Accessor accessor,
      @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2024-01-01")
          @RequestParam(value = "startDate", required = false)
          String startDate,
      @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2024-01-31")
          @RequestParam(value = "endDate", required = false)
          String endDate,
      @Parameter(description = "채널 필터", example = "all")
          @RequestParam(value = "channel", required = false, defaultValue = "all")
          String channel) {
    LocalDate start = parseDate(startDate);
    LocalDate end = parseDate(endDate);

    ScheduledNotificationStatisticsResponse response =
        scheduledNotificationAdminMapper.toStatisticsResponse(
            scheduledNotificationAdminService.getStatistics(start, end, channel));
    return success(response, ResponseMessages.Admin.SCHEDULED_NOTIFICATION_STATISTICS_RETRIEVED);
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException ex) {
      throw new InvalidRequestException("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");
    }
  }
}
