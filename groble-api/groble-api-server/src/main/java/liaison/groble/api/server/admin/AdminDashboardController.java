package liaison.groble.api.server.admin;

import java.time.Duration;
import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.dashboard.response.AdminActiveVisitorsResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardOverviewResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTopContentsResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTrendResponse;
import liaison.groble.api.server.admin.docs.AdminDashboardApiResponses;
import liaison.groble.api.server.admin.docs.AdminDashboardSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dashboard.dto.AdminActiveVisitorsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendDTO;
import liaison.groble.application.admin.dashboard.service.AdminDashboardService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.admin.AdminDashboardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Admin.DASHBOARD_BASE)
@Tag(
    name = AdminDashboardSwaggerDocs.TAG_NAME,
    description = AdminDashboardSwaggerDocs.TAG_DESCRIPTION)
public class AdminDashboardController extends BaseController {

  // Mapper
  private final AdminDashboardMapper adminDashboardMapper;

  // Service
  private final AdminDashboardService adminDashboardService;

  private static final int DEFAULT_TREND_RANGE_DAYS = 30;

  public AdminDashboardController(
      ResponseHelper responseHelper,
      AdminDashboardMapper adminDashboardMapper,
      AdminDashboardService adminDashboardService) {
    super(responseHelper);
    this.adminDashboardMapper = adminDashboardMapper;
    this.adminDashboardService = adminDashboardService;
  }

  // 모든 전체 데이터 조회
  @Operation(
      summary = AdminDashboardSwaggerDocs.DASHBOARD_OVERVIEW_SUMMARY,
      description = AdminDashboardSwaggerDocs.DASHBOARD_OVERVIEW_DESCRIPTION)
  @AdminDashboardApiResponses.GetAdminDashboardOverviewApiResponses
  @Logging(
      item = "AdminDashboard",
      action = "getAdminDashboardOverview",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.DASHBOARD_OVERVIEW)
  public ResponseEntity<GrobleResponse<AdminDashboardOverviewResponse>> getAdminDashboardOverview(
      @Auth Accessor accessor) {
    AdminDashboardOverviewDTO adminDashboardOverviewDTO =
        adminDashboardService.getAdminDashboardOverview();
    AdminDashboardOverviewResponse response =
        adminDashboardMapper.toAdminDashboardOverviewResponse(adminDashboardOverviewDTO);
    return success(response, ResponseMessages.Admin.DASHBOARD_OVERVIEW_RETRIEVED);
  }

  @Operation(
      summary = AdminDashboardSwaggerDocs.DASHBOARD_TREND_SUMMARY,
      description = AdminDashboardSwaggerDocs.DASHBOARD_TREND_DESCRIPTION)
  @AdminDashboardApiResponses.GetAdminDashboardTrendApiResponses
  @Logging(
      item = "AdminDashboard",
      action = "getAdminDashboardTrends",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.DASHBOARD_TRENDS)
  public ResponseEntity<GrobleResponse<AdminDashboardTrendResponse>> getAdminDashboardTrends(
      @Auth Accessor accessor,
      @RequestParam(value = "startDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(value = "endDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
    LocalDate effectiveStartDate =
        startDate != null ? startDate : effectiveEndDate.minusDays(DEFAULT_TREND_RANGE_DAYS - 1L);

    AdminDashboardTrendDTO trendDTO =
        adminDashboardService.getAdminDashboardTrends(effectiveStartDate, effectiveEndDate);
    AdminDashboardTrendResponse response =
        adminDashboardMapper.toAdminDashboardTrendResponse(trendDTO);
    return success(response, ResponseMessages.Admin.DASHBOARD_TRENDS_RETRIEVED);
  }

  @Operation(
      summary = AdminDashboardSwaggerDocs.DASHBOARD_TOP_CONTENTS_SUMMARY,
      description = AdminDashboardSwaggerDocs.DASHBOARD_TOP_CONTENTS_DESCRIPTION)
  @AdminDashboardApiResponses.GetAdminDashboardTopContentsApiResponses
  @Logging(
      item = "AdminDashboard",
      action = "getAdminDashboardTopContents",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.DASHBOARD_TOP_CONTENTS)
  public ResponseEntity<GrobleResponse<AdminDashboardTopContentsResponse>>
      getAdminDashboardTopContents(
          @Auth Accessor accessor,
          @RequestParam(value = "startDate", required = false)
              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
              LocalDate startDate,
          @RequestParam(value = "endDate", required = false)
              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
              LocalDate endDate,
          @RequestParam(value = "limit", required = false) Integer limit) {
    LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
    LocalDate effectiveStartDate =
        startDate != null ? startDate : effectiveEndDate.minusDays(DEFAULT_TREND_RANGE_DAYS - 1L);

    AdminDashboardTopContentsDTO topContentsDTO =
        adminDashboardService.getAdminTopContents(effectiveStartDate, effectiveEndDate, limit);
    AdminDashboardTopContentsResponse response =
        adminDashboardMapper.toAdminDashboardTopContentsResponse(topContentsDTO);
    return success(response, ResponseMessages.Admin.DASHBOARD_TOP_CONTENTS_RETRIEVED);
  }

  @Operation(
      summary = AdminDashboardSwaggerDocs.DASHBOARD_ACTIVE_VISITORS_SUMMARY,
      description = AdminDashboardSwaggerDocs.DASHBOARD_ACTIVE_VISITORS_DESCRIPTION)
  @AdminDashboardApiResponses.GetAdminActiveVisitorsApiResponses
  @Logging(
      item = "AdminDashboard",
      action = "getAdminActiveVisitors",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.DASHBOARD_ACTIVE_VISITORS)
  public ResponseEntity<GrobleResponse<AdminActiveVisitorsResponse>> getAdminActiveVisitors(
      @Auth Accessor accessor,
      @RequestParam(value = "windowMinutes", required = false) @Min(1) @Max(360)
          Integer windowMinutes,
      @RequestParam(value = "limit", required = false) @Min(1) @Max(200) Integer limit) {
    int effectiveWindowMinutes = windowMinutes != null ? windowMinutes : 5;
    int safeWindowMinutes = Math.max(1, Math.min(effectiveWindowMinutes, 360));
    Duration window = Duration.ofMinutes(safeWindowMinutes);

    int effectiveLimit = limit != null ? limit : 50;
    int safeLimit = Math.max(1, Math.min(effectiveLimit, 200));

    AdminActiveVisitorsDTO visitorsDTO = adminDashboardService.getActiveVisitors(window, safeLimit);
    AdminActiveVisitorsResponse response =
        adminDashboardMapper.toAdminActiveVisitorsResponse(visitorsDTO);

    return success(response, ResponseMessages.Admin.DASHBOARD_ACTIVE_VISITORS_RETRIEVED);
  }
}
